---
title: "Claude Code Loop vs Schedule - 반복 자동화, 뭘 써야 할까?"
date: 2026-03-27 12:00:00 +0900
categories: AI-AGENT
tags: [claude-code, loop, schedule, cron, automation, remote-trigger, scheduled-tasks]
excerpt: "Claude Code의 /loop와 /schedule, 둘 다 반복 실행인데 뭐가 다를까? 세션 스코프 vs 클라우드 영구 실행, 내부 동작 원리부터 실전 사용 패턴까지 직접 파헤쳐봤다."
toc: true
toc_sticky: true
---

## 발단: 반복 기능이 두 개?

[이전 글](../claude-code-autonomous-agent-practice)에서 Ralph Wiggum과 Motlin 플러그인으로 자율 에이전트를 돌려봤다. 그런데 Claude Code 자체에도 반복 실행 기능이 생겼다. 그것도 **두 개나**.

```bash
/loop 5m PR 상태 체크해줘
/schedule 매일 아침 9시에 PR 리뷰해줘
```

둘 다 "반복해서 뭔가 해줘"인데, 뭐가 다른 걸까? 직접 써보고 문서를 뒤져보며 정리했다.

## Level 1: `/loop` - 세션 안의 타이머

### 기본 사용법

`/loop`는 현재 열린 Claude Code 세션 안에서 반복 실행되는 타이머다.

```bash
# 기본 문법: /loop [간격] [프롬프트]
/loop 5m 배포 상태 확인해줘
/loop 30m /review-pr 1234
/loop check the build          # 간격 생략 시 기본 10분
```

간격은 앞에 붙여도 되고, `every` 키워드로 뒤에 붙여도 된다:

```bash
/loop 2h 에러 로그 요약해줘
/loop 에러 로그 요약해줘 every 2 hours
```

지원 단위:

| 단위 | 의미 | 비고 |
|------|------|------|
| `s` | 초 | 내부적으로 1분 이상으로 올림 (cron 최소 단위) |
| `m` | 분 | 가장 많이 쓰는 단위 |
| `h` | 시간 | |
| `d` | 일 | |

### 내부 동작: CronCreate/CronList/CronDelete

`/loop`는 사실 세 가지 내부 도구의 래퍼다:

```
/loop 5m PR 체크
    ↓
CronCreate(cron="*/5 * * * *", prompt="PR 체크", recurring=true)
    ↓
Task ID: a1b2c3d4 (8자리)
```

| 도구 | 역할 |
|------|------|
| `CronCreate` | 새 태스크 등록. 5필드 cron 표현식 + 프롬프트 + 반복 여부 |
| `CronList` | 현재 세션의 모든 태스크 목록 조회 |
| `CronDelete` | Task ID로 태스크 삭제 |

세션당 최대 **50개** 태스크를 등록할 수 있다.

### 핵심 특성: 세션 스코프

이게 `/loop`의 가장 중요한 특성이다:

```
터미널 열림 → /loop 등록 → 반복 실행 중... → 터미널 닫힘 → 전부 삭제됨
```

- **터미널(세션)을 닫으면 모든 loop가 사라진다**
- Claude가 다른 작업 중이면 loop 실행이 대기한다 (끼어들지 않음)
- **3일 자동 만료**: 반복 태스크는 생성 후 3일 뒤 자동 삭제
- 시간대는 **로컬 타임존** 기준

### Jitter: 왜 정확한 시간에 안 돌까?

loop가 정확히 5분마다 안 돌고 살짝 밀리는 걸 느낄 수 있다. 이건 의도된 동작이다.

- **반복 태스크**: 주기의 최대 10% 지연, 15분 상한. 1시간 주기면 최대 6분 밀림
- **일회성 태스크**: 정각/반 기준 최대 90초 일찍 실행 가능
- 지연 오프셋은 Task ID 기반이라 **같은 태스크는 항상 같은 오프셋**

### 일회성 리마인더도 된다

`/loop` 없이 자연어로 일회성 알림도 가능하다:

```bash
3시에 릴리즈 브랜치 푸시하라고 알려줘
45분 뒤에 통합 테스트 결과 확인해줘
```

내부적으로 CronCreate에 `recurring=false`로 등록된다.

### /loop 실전 사용 패턴

커뮤니티에서 실제로 많이 쓰는 패턴들:

```bash
# PR 모니터링 - 리뷰 올 때까지 체크
/loop 30m /review-pr 1234

# 배포 추적 - 끝났는지 확인
/loop 5m 배포 상태 확인하고 완료되면 알려줘

# 에러 로그 감시 - 개발 중 실시간 모니터링
/loop 2m logs/server.log 마지막 20줄 확인하고 새 에러 있으면 요약해줘

# 빌드 상태 체크 - CI 결과 기다리기
/loop 5m gh run list --limit 1 결과 확인해줘

# 작업 큐 모니터링
/loop 5m 작업 큐에 남은 아이템 수 확인해줘
```

## Level 2: `/schedule` - 클라우드의 영구 자동화

### /loop와 근본적으로 다른 점

`/schedule`은 세션과 무관하게 **영구적으로 동작하는 예약 태스크**다. 실행 환경이 세 가지로 나뉜다:

| 환경 | 어디서 실행 | PC 필요 | 로컬 파일 접근 |
|------|-----------|--------|--------------|
| CLI (`/schedule`) | Anthropic 클라우드 | X | X (매번 새 clone) |
| Desktop App | 내 PC | O (앱 실행 중) | O |
| Web (`claude.ai/code/scheduled`) | Anthropic 클라우드 | X | X |

### CLI에서 /schedule 사용

```bash
# 기본 사용법
/schedule 매일 아침 9시에 PR 리뷰해줘
/schedule 평일 오전 8시 30분에 Slack 메시지 요약해줘
```

내부적으로 **Remote Trigger**가 생성된다. 이건 Anthropic 클라우드에서 실행되기 때문에:

- **내 PC가 꺼져 있어도 동작한다**
- 매 실행마다 **저장소를 새로 clone** (로컬 변경사항 접근 불가)
- 최소 간격: **1시간**
- 실행 시 권한 프롬프트 없음 (자율 실행)

### Desktop App에서의 Schedule

Desktop App의 Schedule 페이지에서 등록하면 조금 다르다:

- **로컬 파일 접근 가능** (내 PC에서 실행되니까)
- MCP 서버 연결 가능
- 최소 간격: **1분**
- 단, **앱이 실행 중이어야** 동작

### Cron 표현식

`/schedule`은 표준 5필드 cron 표현식을 사용한다:

```
분  시  일  월  요일
*   *   *   *    *
```

| 표현식 | 의미 |
|--------|------|
| `*/5 * * * *` | 5분마다 |
| `0 9 * * *` | 매일 오전 9시 |
| `0 9 * * 1-5` | 평일 오전 9시 |
| `0 */2 * * *` | 2시간마다 |
| `30 14 * * 5` | 매주 금요일 오후 2시 30분 |

요일: `0`(일) ~ `6`(토), `7`도 일요일. **`MON`, `JAN` 같은 이름 별칭은 지원 안 됨.**

### 커넥터: 외부 서비스 연결

`/schedule`의 진짜 힘은 **커넥터(Connector)**에 있다. 외부 서비스를 연결해서 읽고 쓸 수 있다:

```
/schedule 매일 아침 9시
  → GitHub에서 새 PR 확인
  → Linear에서 할당된 이슈 확인
  → Slack으로 요약 보내기
```

지원 커넥터: **Slack, GitHub, Linear** 등. 태스크 생성 시 커넥터를 설정하면 해당 서비스의 데이터를 읽고 결과를 보낼 수 있다.

### /schedule 실전 사용 패턴

```bash
# 매일 아침 PR 리뷰 자동화
/schedule 평일 오전 9시에 열린 PR 리뷰하고 버그, 보안 이슈, 누락된 테스트 확인해줘

# 주간 의존성 감사
/schedule 매주 월요일 오전 10시에 outdated 패키지 확인하고 보안 취약점 리포트 만들어줘

# 모닝 브리핑 (커넥터 활용)
/schedule 평일 오전 8시 30분에 Slack 밤새 메시지 요약, GitHub 새 이슈 정리해줘

# 에러 모니터링 + 자동 PR
/schedule 매시간 에러 로그 분석하고 해결 가능한 에러 있으면 PR 만들어줘
```

## 한눈에 보기: Loop vs Schedule 비교

| 항목 | `/loop` | `/schedule` (클라우드) | Desktop Schedule |
|------|---------|----------------------|-----------------|
| **실행 위치** | 내 PC (터미널 세션) | Anthropic 클라우드 | 내 PC (데스크톱 앱) |
| **PC 필요** | O | X | O (앱 실행 중) |
| **세션 필요** | O | X | X |
| **재시작 후 유지** | X (세션 스코프) | O (영구) | O (영구) |
| **로컬 파일 접근** | O | X (매번 새 clone) | O |
| **MCP 서버** | 세션 설정 상속 | 커넥터로 설정 | 설정 파일 + 커넥터 |
| **최소 간격** | 1분 | 1시간 | 1분 |
| **자동 만료** | 3일 | 없음 | 없음 |
| **세션당 최대** | 50개 | 제한 없음 | 제한 없음 |
| **컨텍스트** | 세션 내 누적 | 매번 새로 시작 | 매번 새로 시작 |
| **토큰 소비** | 대기 중에도 세션 유지 | 실행 시에만 소비 | 실행 시에만 소비 |
| **권한 프롬프트** | 세션 설정 상속 | 없음 (자율) | 태스크별 설정 |

### 아키텍처 차이 핵심

```
/loop                              /schedule (클라우드)
┌──────────────────────┐           ┌──────────────────────┐
│  터미널 세션           │           │  Anthropic Cloud      │
│  ┌────────────────┐  │           │  ┌────────────────┐  │
│  │ Claude Code    │  │           │  │ Remote Trigger  │  │
│  │ (컨텍스트 누적) │  │           │  │ (매번 새 컨텍스트)│  │
│  │                │  │           │  │                 │  │
│  │ cron → 실행    │  │           │  │ cron → clone    │  │
│  │   ↓            │  │           │  │   ↓             │  │
│  │ 결과 → 세션에  │  │           │  │ 실행 → 결과     │  │
│  │       누적     │  │           │  │   ↓             │  │
│  └────────────────┘  │           │  │ 커넥터로 전송   │  │
│                       │           │  └────────────────┘  │
│  터미널 닫으면 끝 ❌   │           │  PC 꺼도 계속 ✅     │
└──────────────────────┘           └──────────────────────┘
```

1. **상태 유지**: `/loop`는 세션 안에서 컨텍스트가 쌓인다. `/schedule`은 매번 백지 상태
2. **비용 효율**: `/loop`는 대기 중에도 세션을 점유. `/schedule`은 실행할 때만 토큰 소비
3. **반응 속도**: `/loop`는 초 단위 반응 가능. `/schedule`은 정해진 시각에만 실행
4. **생존 범위**: `/loop`는 터미널 닫으면 끝. `/schedule`은 수동 삭제 전까지 영구

## 어떤 상황에 뭘 써야 할까?

### `/loop`를 쓰는 경우

> 지금 내가 작업 중이고, 잠깐 동안 뭔가를 반복 체크하고 싶을 때

- 배포 끝날 때까지 5분마다 상태 확인
- PR 리뷰 올 때까지 30분마다 체크
- 디버깅하면서 에러 로그 2분마다 감시
- 빌드 CI 결과 기다리기

**특징**: 짧은 수명, 즉각 반응, 로컬 파일 접근 가능

### `/schedule`을 쓰는 경우

> 내가 안 봐도 정해진 시간에 자동으로 돌아갔으면 좋겠을 때

- 매일 아침 9시 PR 리뷰
- 주간 의존성 보안 감사
- 평일 아침 Slack 브리핑
- 매시간 에러 모니터링 → 자동 수정 PR

**특징**: 장기 운영, PC 불필요(클라우드), 커넥터로 외부 서비스 연동

### Desktop Schedule을 쓰는 경우

> 로컬 파일에 접근해야 하는데, 세션 유지는 귀찮을 때

- 커밋 안 된 로컬 변경사항 기반 작업
- MCP 서버와 연동된 자동화
- 1시간 미만 간격으로 자주 실행해야 할 때

### 의사결정 흐름

```
반복 자동화가 필요하다
    │
    ├─ 지금 당장, 잠깐만? ──────────→ /loop
    │
    ├─ 매일/매주 정기적으로? ─────┐
    │                             │
    │   ├─ 로컬 파일 필요? ───────→ Desktop Schedule
    │   │
    │   └─ 클라우드로 충분? ──────→ /schedule (Remote Trigger)
    │
    └─ 1시간 미만 간격 + 영구? ──→ Desktop Schedule
```

## 정리: 미래의 나에게

### TL;DR

- **`/loop`**: 현재 세션 안의 타이머. 터미널 닫으면 끝. 3일 만료. 세션당 50개. 로컬 파일 접근 O
- **`/schedule`**: 클라우드 영구 예약. PC 꺼도 동작. 커넥터로 Slack/GitHub 연동. 최소 1시간 간격
- **Desktop Schedule**: 로컬 실행 영구 예약. 앱 실행 필요. MCP 연동 가능. 최소 1분 간격
- **선택 기준**: 일시적 모니터링 → `/loop`, 정기 자동화 → `/schedule`, 로컬+영구 → Desktop

### 복붙용 Quick Start

```bash
# /loop - 바로 쓰기
/loop 5m 배포 상태 확인해줘
/loop 30m /review-pr 1234
/loop 2m logs/server.log 에러 확인해줘

# /schedule - 정기 예약
/schedule 평일 오전 9시에 열린 PR 리뷰해줘
/schedule 매주 월요일 오전 10시에 의존성 보안 체크해줘
/schedule 매일 오전 8시 30분에 Slack 메시지 요약해줘
```

### 주의할 점

- `/loop`는 **세션 스코프**다. 터미널 닫으면 전부 사라지고, 최대 3일까지만 유지된다
- `/schedule` 클라우드는 매번 **저장소를 새로 clone**한다. 커밋 안 된 로컬 변경사항은 보이지 않는다
- `/loop`는 대기 중에도 세션을 점유하므로, 장시간 돌리면 토큰 비용이 쌓일 수 있다
- cron 표현식에서 `MON`, `JAN` 같은 이름 별칭은 지원 안 된다. 숫자만 사용할 것
- `/loop`의 실행 시간은 jitter 때문에 정확하지 않다. 정밀한 타이밍이 필요하면 `/schedule` 사용

### 더 파볼 거리

- [Run prompts on a schedule - Claude Code 공식 문서](https://docs.anthropic.com/en/docs/claude-code/scheduled-tasks)
- [Claude Code Desktop - Scheduled Tasks](https://docs.anthropic.com/en/docs/claude-code/desktop)
- Ralph Wiggum, Motlin 플러그인과 `/loop`/`/schedule`을 조합한 하이브리드 자동화
- MCP 서버를 커넥터 대신 사용하는 Desktop Schedule 패턴

## References

- [Run prompts on a schedule - Claude Code Docs](https://docs.anthropic.com/en/docs/claude-code/scheduled-tasks)
- [Schedule tasks on the web - Claude Code Docs](https://docs.anthropic.com/en/docs/claude-code/web-scheduled-tasks)
- [Claude Code Desktop](https://docs.anthropic.com/en/docs/claude-code/desktop)
- [Claude Code Loop vs Scheduled Tasks - MindStudio](https://www.mindstudio.ai/blog/claude-code-loop-vs-scheduled-tasks)
- [What Is Claude Code Loop? - MindStudio](https://www.mindstudio.ai/blog/what-is-claude-code-loop-scheduled-recurring-tasks)
- [Claude Code Gets Cron Scheduling - WinBuzzer](https://winbuzzer.com/2026/03/09/anthropic-claude-code-cron-scheduling-background-worker-loop-xcxwbn/)
- [Recurring Tasks in Claude Code - Better Stack Community](https://betterstack.com/community/guides/ai/claude-code-loop/)
- [이전 글: 자율 에이전트 실전 가이드 (2편)](../claude-code-autonomous-agent-practice)
