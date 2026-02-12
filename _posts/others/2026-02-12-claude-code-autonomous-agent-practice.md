---
title: "Claude Code 자율 에이전트 (2) - Ralph Wiggum과 Motlin 플러그인 실전 가이드"
date: 2026-02-12 14:00:00 +0900
categories: OTHERS
tags: [claude, ai, autonomous-agent, ralph-wiggum, motlin, claude-code-plugins, puppeteer, cost-management]
excerpt: "Ralph Wiggum과 Craig Motlin의 markdown-tasks 플러그인을 직접 클론해서 소스코드를 까봤다. 설치부터 실행, 안전장치까지 실전 사용법을 정리한다."
toc: true
toc_sticky: true
---

## 발단: 직접 클론해서 까보자

[이전 글](../claude-code-autonomous-agent-concept)에서 자율 에이전트의 설계 원리를 다뤘다. 이제 실제 도구를 써볼 차례다.

대표적인 도구 두 가지를 직접 클론해서 소스코드를 뜯어봤다.

```bash
git clone https://github.com/frankbria/ralph-claude-code.git
git clone https://github.com/motlin/claude-code-plugins.git
```

`ralph_loop.sh` 1,794줄과 Motlin 플러그인 14개를 전부 읽으며 파악한 실제 동작 방식을 정리한다.

## Level 1: Ralph Wiggum - 실제 동작 방식

### 동작 원리: while true 루프

Ralph의 핵심 구조를 `ralph_loop.sh`에서 직접 확인해보면 이렇다.

```bash
# ralph_loop.sh의 핵심 구조 (실제 코드 기반 요약)
while true; do
    # .ralph/PROMPT.md 파일에서 프롬프트를 읽어서
    PROMPT=$(cat .ralph/PROMPT.md)

    # Claude Code CLI를 서브프로세스로 실행
    claude -p "$PROMPT" --output-format json --resume "$SESSION_ID"

    # JSON 응답에서 종료 조건 판단 (dual-condition gate)
    # 1) completion_indicators >= 2
    # 2) EXIT_SIGNAL: true

    # 두 조건 모두 충족 → 루프 탈출
    # 아니면 → 다음 반복
done
```

핵심: **bash `while true` 루프 안에서 Claude CLI를 반복 실행하는 구조**다. Claude가 끝나면 종료를 확인하고 다시 시작한다.

### 실제 설치 및 사용법

```bash
# 1. 클론 및 설치
git clone https://github.com/frankbria/ralph-claude-code.git
cd ralph-claude-code
./install.sh
```

설치하면 `$HOME/.local/bin/`에 7개 명령어가 생긴다:

| 명령어 | 역할 |
|--------|------|
| `ralph` | 메인 루프 실행 |
| `ralph-monitor` | 모니터링 대시보드 |
| `ralph-setup` | 새 프로젝트 생성 |
| `ralph-import` | PRD 문서 변환 |
| `ralph-enable` | 기존 프로젝트에 Ralph 활성화 |
| `ralph-enable-ci` | CI용 비인터랙티브 활성화 |
| `ralph-migrate` | 기존 프로젝트 마이그레이션 |

**사전 요구사항**: Node.js(npx), jq, git, coreutils(macOS: `brew install coreutils`), tmux(권장)

```bash
# 2. 기존 프로젝트에 Ralph 활성화
cd my-project
ralph-enable
# → .ralph/ 디렉토리 생성 (PROMPT.md, AGENT.md, specs/ 등)

# 3. 실행
ralph --monitor    # tmux 3-pane 모니터링 포함 (권장)
ralph              # 루프만 실행
ralph-monitor      # 별도 터미널에서 모니터링 대시보드
```

### 실제 CLI 옵션

`ralph_loop.sh`의 `show_help` 함수에서 확인한 옵션들:

```bash
ralph [OPTIONS]

# 기본 옵션
  -c, --calls NUM       시간당 최대 호출 수 (기본값: 100)
  -p, --prompt FILE     프롬프트 파일 지정 (기본값: .ralph/PROMPT.md)
  -m, --monitor         tmux 통합 모니터링 시작
  -v, --verbose         상세 출력
  -l, --live            실시간 스트리밍 출력
  -t, --timeout MIN     실행 타임아웃 (1-120분, 기본값: 15)

# 안전장치
  --allowed-tools TOOLS  허용 도구 목록
  --reset-circuit       회로 차단기 리셋
  --circuit-status      회로 차단기 상태 조회
  --session-expiry HRS  세션 만료 시간 (기본값: 24시간)
```

반복 제한은 `--calls`로 시간당 호출 수를 제한하는 방식이고, 완료 조건은 dual-condition gate로 자동 판단된다.

### Dual-Condition Exit Gate

Ralph의 종료 조건이 꽤 정교하다:

```
종료하려면 두 조건 모두 충족 필요:
1. completion_indicators >= 2  (자연어 패턴에서 탐지)
2. Claude의 명시적 EXIT_SIGNAL: true 선언
```

한쪽만 충족되면 루프가 계속된다. 이중 조건이라 **실수로 일찍 끝나는 것을 방지**한다.

추가 안전장치:
- **회로 차단기(Circuit Breaker)**: 진전 없음 3회 연속, 동일 에러 5회 연속 감지 시 자동 중단
- **Rate Limiting**: 시간당 100회 기본 제한
- **5시간 API 제한 감지**: Anthropic의 rate limit에 걸리면 감지
- **권한 거부 감지**: 도구 사용 권한이 없을 때 감지

### 모니터링 대시보드

`ralph-monitor`를 실행하면 2초마다 갱신되는 대시보드가 뜬다:

```
┌─ Current Status ──────────────────┐
│ Loop Count: 42/100                │
│ Status: running                   │
│ API Calls: 45/100 (hourly)        │
├─ Recent Activity ─────────────────┤
│ [13:42] Feature: 사용자 프로필 구현  │
│ [13:38] Test: 3/3 passed          │
│ [13:35] Commit: feat: add profile │
└───────────────────────────────────┘
```

`ralph --monitor`로 실행하면 tmux로 3-pane 레이아웃이 구성된다:
- 왼쪽: Ralph 루프 실행
- 오른쪽 상단: 실시간 Claude 출력 (`tail -f .ralph/live.log`)
- 오른쪽 하단: 상태 대시보드

## Level 2: Craig Motlin의 markdown-tasks 플러그인

### Craig Motlin이 만든 플러그인

**Craig P. Motlin**은 Eclipse Collections(구 GS Collections) 자바 컬렉션 라이브러리의 기술 리드 출신 소프트웨어 엔지니어다. 자신의 블로그 [motlin.com](https://motlin.com)에서 Claude Code 관련 워크플로우를 공유하고 있다.

그가 만든 것은 `claude-code-plugins`라는 Claude Code 플러그인 모음이고, 그 중 **`markdown-tasks`**가 하위 에이전트 기반 태스크 관리 플러그인이다.

### 실제 플러그인 구조

직접 클론해서 확인한 구조:

```
claude-code-plugins/plugins/markdown-tasks/
├── agents/
│   └── do-task.md              # 하위 에이전트 정의 (핵심)
├── commands/
│   ├── add-one-task.md         # /add-one-task
│   ├── do-one-task.md          # /do-one-task
│   ├── do-all-tasks.md         # /do-all-tasks (자율 루프)
│   ├── count-tasks.md          # /count-tasks
│   ├── plan-tasks.md           # /plan-tasks
│   ├── import-plan.md          # /import-plan
│   └── sweep-todos.md          # /sweep-todos
├── scripts/
│   ├── task_get.py             # 미완료 태스크 추출
│   ├── task_complete.py        # 완료 마킹 [ ] → [x]
│   ├── task_add.py             # 태스크 추가
│   └── task_archive.py         # 완료 목록 아카이브
├── skills/tasks/SKILL.md       # 스킬 정의
└── plugin.json                 # 플러그인 메타데이터 (v1.4.1)
```

주요 포인트:
- 태스크 파일: **`.llm/todo.md`**에 저장
- 하위 에이전트: **`do-task`**
- 상태 기록: **`todo.md` 자체의 체크박스**를 Python 스크립트로 수정

### 설치법

```bash
# 방법 1: 마켓플레이스 (원격)
claude plugin marketplace add motlin/claude-code-plugins
claude plugin install markdown-tasks@motlin-claude-code-plugins

# 방법 2: 로컬 클론 후 설치
git clone https://github.com/motlin/claude-code-plugins.git
cd claude-code-plugins
./install-local.sh
```

사전 요구사항: Python 3 (`doctor.sh`로 검사 가능)

### `do-task` 에이전트의 실제 워크플로우

`agents/do-task.md`에서 확인한 실제 6단계:

```
1. Extract   → task_get.py로 .llm/todo.md에서 첫 번째 [ ] 태스크 추출
2. Implement → 해당 태스크만 집중 구현
3. Verify    → build:precommit-runner 에이전트로 빌드 검증
4. Commit    → git:commit-handler 에이전트로 커밋
5. Rebase    → git:rebaser 에이전트로 리베이스
6. Complete  → task_complete.py로 [ ] → [x] 변경
```

주목할 점은, `do-task` 에이전트 자체는 **태스크 하나만 처리하고 끝난다.** 반복(루프)은 이를 호출하는 `/do-all-tasks` 커맨드가 담당한다. 에이전트와 오케스트레이터의 역할이 깔끔하게 분리되어 있다.

### 태스크 상태 관리

`.llm/todo.md` 파일의 마크다운 체크박스로 관리:

```markdown
## 구현할 기능들

- [ ] 사용자 인증 시스템 구현
- [x] 대시보드 레이아웃 완성
- [>] API 엔드포인트 개발 중
- [!] 결제 시스템 연동 (실패 - 외부 API 문제)
```

| 상태 | 의미 |
|------|------|
| `[ ]` | 미완료 (작업 가능) |
| `[x]` | 완료 |
| `[>]` | 진행 중 |
| `[!]` | 차단됨 (실패 후) |

완료된 태스크는 `task_archive.py`가 `.llm/YYYY-MM-DD-todo.md`로 자동 아카이브한다.

### 컨텍스트 격리의 실제 효과

이 플러그인의 핵심 가치는 **컨텍스트 격리**다. `do-task` 에이전트의 빌드 로그, 테스트 출력, git 작업 등이 **메인 대화의 컨텍스트를 소비하지 않는다.**

```
메인 세션 (오케스트레이터)          do-task 에이전트 (격리)
┌─────────────────────┐         ┌─────────────────────┐
│ /do-all-tasks 실행    │────────▶│ task_get.py 실행     │
│                      │         │ 코드 구현            │
│ (컨텍스트 깔끔 유지)   │         │ npm run build ← 로그 │
│                      │         │ npm test ← 로그      │
│                      │◀────────│ [x] 완료 마킹        │
│ 다음 태스크 위임       │────────▶│ ...                  │
└─────────────────────┘         └─────────────────────┘
```

Craig Motlin 본인이 이 방식으로 **2시간 이상 무인 실행**에 성공했다고 블로그에서 밝히고 있다.

### builtin-tasks: 또 다른 선택지

같은 저장소에 `builtin-tasks`라는 별도 플러그인도 있다. 이건 마크다운 파일 대신 Claude Code **내장 Task 도구** (`TaskCreate`, `TaskList`, `TaskGet`, `TaskUpdate`)를 사용한다.

- **markdown-tasks**: `.llm/todo.md` 파일 기반, Python 스크립트로 조작
- **builtin-tasks**: Claude Code 내장 도구 기반, `TeamCreate`로 병렬 처리 가능

프로젝트 성격에 따라 선택하면 된다.

## 비용과 안전: 자율 에이전트를 방치하면 안 되는 이유

### 비용 구조

| 사용 방식 | 과금 모델 | 24시간 가동 비용 |
|----------|----------|----------------|
| API 키 (`ANTHROPIC_API_KEY`) | 토큰당 종량제 | 사용량에 비례 (예측 어려움) |
| Claude Max 5x 구독 | 월 $100 정액 | 정액 내 사용 가능 |
| Claude Max 20x 구독 | 월 $200 정액 | 정액 내 사용 가능 |

**주의: 구독이 "무제한"은 아니다.** 직접 확인한 제한 사항:
- **5시간 롤링 윈도우** 토큰 한도 존재
- **7일 주간 사용량 상한** 존재 (2025년 8월 도입)
- 모델별 차등 제한 (Opus가 Sonnet보다 엄격)
- 한도 초과 시 Extra Usage 활성화하면 표준 API 요금으로 추가 과금

그래도 종량제 API 대비 **비용 예측 가능성이 높아**서, 24시간 가동 시에는 구독이 경제적이다.

### 안전장치 체크리스트

자율 에이전트를 돌리기 전에 반드시 설정해야 할 것들:

```bash
# Ralph의 경우
ralph --calls 50              # 시간당 호출 제한 (기본 100 → 50으로 낮추기)
ralph --timeout 15            # 개별 실행 타임아웃 (분)
ralph --allowed-tools "Write,Read,Edit,Bash(git *),Bash(npm *)"  # 도구 제한
```

| 안전장치 | Ralph 지원 | Motlin 지원 | 설명 |
|---------|-----------|------------|------|
| 호출 수 제한 | `--calls NUM` | N/A | 시간당 최대 API 호출 |
| 회로 차단기 | 내장 (3회 no-progress) | N/A | 연속 실패 시 자동 중단 |
| 도구 제한 | `--allowed-tools` | N/A | 사용 가능 도구 화이트리스트 |
| 세션 만료 | `--session-expiry` | N/A | 기본 24시간 |
| 실패 마킹 | N/A | `[!]` 마킹 | 실패한 태스크 건너뛰기 |
| 알림 | 별도 설정 필요 | `pushover` 플러그인 | 완료/실패 시 푸시 알림 |

### Pushover 알림 연동

Motlin 플러그인에는 `pushover` 플러그인이 포함되어 있다. 설정하면 태스크 완료/실패 시 휴대폰으로 푸시 알림을 받을 수 있다.

Ralph의 경우 별도 스크립트로 연동해야 한다:

```bash
#!/bin/bash
# ralph 완료/실패 감지 후 알림
ralph --monitor 2>&1 | while read line; do
  if echo "$line" | grep -q "COMPLETED\|FAILED\|CIRCUIT_OPEN"; then
    curl -s -F "token=$PUSHOVER_TOKEN" \
         -F "user=$PUSHOVER_USER" \
         -F "message=$line" \
         https://api.pushover.net/1/messages.json
  fi
done
```

## 소스코드를 읽으며 배운 것들

### 컨텍스트 격리의 체감 효과

Level 1(Ralph, 단순 루프)과 Level 2(Motlin, 하위 에이전트 격리)의 차이는 이론으로는 와닿지 않는다. 실제로 돌려보면:

- **Level 1**: 빌드 로그가 쌓이면서 세션 후반에 Claude가 이전 맥락을 잃기 시작
- **Level 2**: 빌드 로그가 하위 에이전트에 격리되니 메인 세션이 계속 깔끔

특히 테스트 실패 로그가 많이 나오는 프로젝트에서 차이가 극명하다.

### 개발자의 역할이 바뀐다

자율 에이전트를 세팅하고 나면, 개발자가 하는 일이 달라진다:

| 기존 | 자율 에이전트 환경 |
|------|------------------|
| 코드를 직접 작성 | 에이전트가 따를 사양(Spec)을 작성 |
| 빌드하고 테스트 | 에이전트의 검증 파이프라인을 설계 |
| 버그를 직접 수정 | 에이전트가 실패한 태스크를 리뷰 |
| git 히스토리 관리 | 에이전트의 커밋 품질을 모니터링 |

**"구현자"에서 "감독자(Supervisor)"로의 전환.** 코드를 잘 짜는 것보다 에이전트가 잘 돌아가도록 워크플로우를 설계하는 능력이 중요해진다.

## 정리: 미래의 나에게

### TL;DR

- **Ralph Wiggum**: `ralph --monitor`로 실행, `.ralph/PROMPT.md`에 프롬프트, dual-condition gate로 종료 판단
- **Motlin 플러그인**: `markdown-tasks` 플러그인, `.llm/todo.md`로 태스크 관리, `do-task` 에이전트가 1개씩 처리
- **비용**: Max 5x($100/월) 구독이 경제적이나 무제한은 아님. 5시간 윈도우 + 주간 상한 존재
- **안전**: `--calls`, `--allowed-tools`, 회로 차단기 반드시 설정

### 복붙용 Quick Start

```bash
# Ralph 시작하기
git clone https://github.com/frankbria/ralph-claude-code.git
cd ralph-claude-code && ./install.sh
cd /path/to/my-project && ralph-enable
ralph --monitor

# Motlin 플러그인 시작하기
claude plugin marketplace add motlin/claude-code-plugins
claude plugin install markdown-tasks@motlin-claude-code-plugins
# 이후 /plan-tasks → /do-all-tasks
```

### 주의할 점

- 도구를 쓰기 전에 README와 소스코드를 직접 확인하자. 버전업에 따라 인터페이스가 자주 바뀐다
- Max 구독이 "무제한"은 아니다. 5시간 롤링 윈도우 + 주간 상한 존재
- 자율 에이전트를 완전히 방치하지 말 것. 모니터링 + 알림은 필수

### 더 파볼 거리

- Craig Motlin 블로그의 실전 경험담: [Claude Code: Keeping It Running for Hours](https://motlin.com/blog/claude-code-running-for-hours)
- Anthropic 공식 하위 에이전트 문서: [Create custom subagents](https://code.claude.com/docs/en/sub-agents)
- MCP(Model Context Protocol)를 활용한 에이전트 간 통신 아키텍처
- Puppeteer MCP vs Playwright MCP 비교

## References

- [Ralph for Claude Code - GitHub](https://github.com/frankbria/ralph-claude-code) (v0.11.4, 소스코드 직접 분석)
- [Craig Motlin's Claude Code Plugins - GitHub](https://github.com/motlin/claude-code-plugins) (v1.4.1, 소스코드 직접 분석)
- [Craig Motlin: Claude Code - Moving from Slash Commands to Agents](https://motlin.com/blog/claude-code-slash-commands-to-agents)
- [Craig Motlin: Claude Code - Keeping It Running for Hours](https://motlin.com/blog/claude-code-running-for-hours)
- [Effective harnesses for long-running agents - Anthropic Engineering](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
- [Claude Code 비용 관리 문서](https://code.claude.com/docs/en/costs)
- [Claude 유료 플랜 Extra Usage - Claude Help Center](https://support.claude.com/en/articles/12429409-extra-usage-for-paid-claude-plans)
- [이전 글: 자율 에이전트의 원리 (1편)](../claude-code-autonomous-agent-concept)
