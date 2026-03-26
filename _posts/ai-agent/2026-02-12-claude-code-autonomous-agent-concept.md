---
title: "Claude Code 자율 에이전트 (1) - 잠자는 동안 코드가 쌓이는 원리"
date: 2026-02-12 10:00:00 +0900
categories: AI-AGENT
tags: [claude, ai, autonomous-agent, agentic-loop, context-window, anthropic-research]
excerpt: "Claude Code는 할 일이 끝나면 멈춘다. 그런데 멈추지 않고 밤새 코드를 짜게 만들 수 있다면? Anthropic 연구 원문을 직접 파헤치며, 자율 에이전트의 설계 원리를 검증해봤다."
toc: true
toc_sticky: true
---

## 발단: "Claude야, 너 왜 자꾸 멈추는 거야?"

[지난 글](../claude-code-java-setup)에서 Claude Code에 자바 DNA를 심는 방법을 알아봤다. Skills로 무장시키니 꽤 쓸만한 페어 프로그래머가 됐다.

그런데 어느 날 퇴근 전에 이런 생각이 들었다.

> "내가 자는 동안에도 Claude가 알아서 기능을 구현하고 커밋하면 안 되나?"

아침에 출근해서 `git log`를 치면 밤새 20개의 커밋이 쌓여있는 상상. 실제로 이런 시스템을 구축하고 운영하는 사례들이 나오고 있다.

문제는 Claude Code의 근본적인 한계에 있었다.

```
[나] "이 기능 구현해줘"
[Claude] 구현 완료! 다른 도움이 필요하신가요?
[나] (자리 비움)
[Claude] ... (세션 종료)
```

Claude Code는 **Stateless**하다. 목표에 도달했다고 판단하거나, 사람의 입력이 필요하면 즉시 멈춘다. 24시간 가동? 표준 세션으로는 불가능하다.

이 문제를 어떻게 극복하는지, 직접 자료를 파헤치고 소스코드까지 뜯어보며 검증해봤다.

## 핵심 원리: 에이전트 루프(Agentic Loop)

Anthropic의 연구팀은 이 문제를 이렇게 비유했다.

> "교대 근무하는 엔지니어들로 구성된 프로젝트를 상상해보라. 새로 온 엔지니어는 이전 교대에 무슨 일이 있었는지 전혀 기억하지 못한다."
>
> -- [Effective harnesses for long-running agents](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents), Anthropic Engineering

해결의 핵심은 **Claude 바깥에서 Claude를 제어하는 것**이다.

```
┌──────────────────────────────────────┐
│        외부 제어 계층 (Shell)          │
│                                       │
│  ┌─────────┐   ┌──────────────────┐  │
│  │ 상태 파일 │◄──│ Claude Code      │  │
│  │ (JSON)   │   │ 세션 #1          │  │
│  └─────────┘   └───────┬──────────┘  │
│       │                 │ (종료)       │
│       │        ┌────────▼─────────┐  │
│       │        │ 종료 감지          │  │
│       │        │ → 다음 작업 확인   │  │
│       │        │ → 새 세션 시작     │  │
│       │        └────────┬─────────┘  │
│       │        ┌────────▼─────────┐  │
│       ▼────────│ Claude Code      │  │
│                │ 세션 #2          │  │
│                └──────────────────┘  │
└──────────────────────────────────────┘
```

이 구조를 **에이전트 루프(Agentic Loop)**라고 부른다. 원리는 세 가지다:

1. **종료 감지**: Claude가 세션을 끝내면 외부 스크립트가 이를 감지
2. **상태 영속화**: 작업 목록과 진행 상황을 외부 파일(JSON, Markdown)에 보관
3. **컨텍스트 격리**: 매 작업을 새로운 세션에서 시작해 컨텍스트 오염 방지

여기서 3번이 특히 중요하다. 왜 그런지 설명하겠다.

## 컨텍스트 오염: 장기 에이전트의 숨은 적

처음에는 "하나의 세션을 계속 유지하면 되지 않나?"라고 생각했다. 틀렸다.

Claude Code의 컨텍스트 윈도우는 유한하다. 장기 실행 세션에서 무슨 일이 벌어지는지 보자:

```
세션 시작: 컨텍스트 사용량 5%
├── 기능 1 구현: 15%
├── 빌드 로그 출력: 25%  ← 로그가 컨텍스트를 먹기 시작
├── 테스트 실행: 40%
├── 기능 2 구현: 55%
├── 빌드 실패 로그: 70%  ← 에러 메시지가 컨텍스트를 잡아먹음
├── 디버깅 시도: 85%
└── 기능 3 구현: 95%    ← 추론 정확도 급격히 저하
```

빌드 로그, 테스트 출력, 에러 메시지... 이런 것들이 컨텍스트 윈도우를 급격히 소모한다. 그 결과:

- 이전에 뭘 구현했는지 잊어버림
- 같은 버그를 반복해서 만듦
- 코드 품질이 세션 후반으로 갈수록 떨어짐

이게 바로 **컨텍스트 오염(Context Pollution)**이다. 해결책은 작업 단위마다 세션을 **의도적으로 끊는 것**이다. 반직관적이지만 이게 핵심이다.

## Anthropic 연구 원문: 실제로 뭘 제안했나

여기서부터가 중요하다. Anthropic의 원문을 직접 읽어보고 정리한 구조는 이렇다.

### 2-파트 하네스 아키텍처

Anthropic이 제안한 구조는 **두 종류의 에이전트 세션**으로 구성된다.

```
[App Spec 문서]
      │
      ▼
┌─────────────────────┐
│ Initializer Agent    │  ← 최초 1회만 실행
│                      │
│ 1. init.sh 생성      │  (개발 서버 자동 실행 스크립트)
│ 2. progress.txt 생성 │  (작업 이력 문서)
│ 3. 초기 git commit   │
│ 4. features.json 생성│  (200개+ 원자적 기능 목록)
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Coding Agent         │  ← 이후 모든 세션에서 반복
│                      │
│ 1. pwd 확인          │
│ 2. git log +         │
│    progress 파일 읽기 │
│ 3. features.json에서 │
│    최상위 미완료 선택  │
│ 4. 구현 + 검증       │
│ 5. git commit        │
│ 6. progress 업데이트  │
└─────────────────────┘
     ↑         │
     └─────────┘  (세션 종료 → 새 세션 시작 → 반복)
```

### Features List: 왜 Markdown이 아니라 JSON인가

이게 재미있었다. Anthropic이 feature 추적에 **Markdown 대신 JSON**을 선택한 이유:

> "모델이 JSON 파일을 부적절하게 변경하거나 덮어쓸 가능성이 Markdown보다 낮다."

실제 구조:

```json
{
  "category": "functional",
  "description": "New chat button creates a fresh conversation",
  "steps": [
    "Navigate to main interface",
    "Click the 'New Chat' button",
    "Verify a new conversation is created",
    "Check that chat area shows welcome state",
    "Verify conversation appears in sidebar"
  ],
  "passes": false
}
```

규칙은 간단하다: 에이전트는 **오직 `passes` 필드만** 수정해야 한다. 테스트를 삭제하거나 수정하는 것은 금지. 원문의 표현을 빌리면:

> "테스트를 삭제하거나 편집하는 것은 허용할 수 없다. 이는 기능 누락이나 버그로 이어질 수 있기 때문이다."

### Puppeteer MCP를 활용한 자율 검증

에이전트가 인간 없이 자기 작업을 검증하는 방법이다.

```
새 기능 구현 전
    │
    ▼
[Puppeteer MCP로 기존 기능 end-to-end 테스트]
    │
    ├── PASS → 새 기능 구현 시작
    │
    └── FAIL → 기존 기능 수정부터 처리
```

Puppeteer MCP를 사용하면 에이전트가 실제 브라우저에서 버튼을 클릭하고, 페이지를 탐색하고, 스크린샷을 찍어서 기능을 확인할 수 있다. 코드 리뷰만으로는 보이지 않는 버그를 실제 사용자 관점에서 발견하는 데 유효하다.

다만 한계도 있다. 원문에 따르면 Claude가 **브라우저 네이티브 alert 모달**을 감지하지 못하는 경우가 있었다고 한다.

### 4가지 실패 모드와 해결 전략

Anthropic이 실제 실험에서 관찰한 실패 패턴과 그 해결법이다. 이게 실무에서 가장 유용한 부분이었다.

| 실패 모드 | Initializer Agent 해결 | Coding Agent 해결 |
|-----------|----------------------|-------------------|
| **너무 일찍 "완료" 선언** | end-to-end 검증 포함된 feature list 생성 | 한 번에 하나의 feature만 작업 |
| **환경이 버그 투성이로 방치** | 초기 git repo + progress 노트 작성 | 세션 시작 시 progress + git log 읽기 |
| **feature를 성급하게 완료 처리** | feature list 파일 설정 | self-verify 후에만 `passes: true` |
| **앱 셋업 파악에 시간 낭비** | `init.sh` 자동 실행 스크립트 작성 | 세션 시작 시 `init.sh` 읽기 |

"너무 일찍 완료 선언"이 가장 흔한 문제라는 게 인상적이었다. 에이전트가 "구현 완료!"라고 표시해놓고 실제로는 절반만 된 경우가 많았다고 한다. 그래서 Puppeteer로 **self-verify**하는 단계가 필수인 것이다.

## 에이전트 성숙도 3단계

이 연구를 포함해서 커뮤니티에서 사용되는 자율 에이전트 접근법을 크게 세 단계로 나눌 수 있다.

| 단계 | 접근법 | 컨텍스트 관리 | 적합한 작업 |
|------|--------|-------------|------------|
| **Level 1** | 단순 반복 루프 | 세션 유지 (격리 없음) | 리팩토링, 테스트 보강 |
| **Level 2** | 하위 에이전트 격리 | 하위 세션에서 로그 격리 | 중소규모 기능 구현 |
| **Level 3** | 사양 기반 자율 루프 | 세션 단위 완전 격리 + JSON 상태 | 풀스택 앱 구축 |

- **Level 1**: Ralph Wiggum 같은 도구가 bash `while true` 루프로 Claude를 반복 실행
- **Level 2**: Craig Motlin의 markdown-tasks 플러그인처럼 하위 에이전트에게 작업을 위임
- **Level 3**: Anthropic 연구의 2-파트 하네스처럼 Initializer + Coding Agent 구조

각 도구의 실제 사용법과 소스코드 분석은 [다음 글 (실전편)](../claude-code-autonomous-agent-practice)에서 다룬다.

## 핵심 설계 원칙: 좋은 엔지니어가 하는 일을 에이전트에게 시키기

Anthropic 연구팀이 밝힌 설계 영감의 출처가 인상적이었다:

> "이러한 실천법의 영감은 효과적인 소프트웨어 엔지니어가 매일 하는 일을 아는 것에서 왔다."

생각해보면 당연하다:

- **좋은 개발자**: 출근하면 Jira 보고 → git log 확인 → 현재 상태 파악 → 코딩 시작
- **좋은 에이전트**: 세션 시작하면 features.json 읽고 → git log 확인 → progress 파악 → 코딩 시작

- **좋은 개발자**: 기능 만들면 테스트 돌리고 → 리뷰 받고 → 커밋
- **좋은 에이전트**: 기능 만들면 Puppeteer로 self-verify → passes 업데이트 → 커밋

자율 에이전트의 설계는 결국 **"좋은 개발자의 습관을 자동화하는 것"**이었다.

## 정리: 미래의 나에게

### TL;DR

- Claude Code는 Stateless → **외부 루프**로 24시간 가동 가능
- 핵심 3요소: 종료 감지 + 상태 파일 영속화 + **컨텍스트 격리**
- Anthropic 연구의 실제 구조: **2-파트 하네스** (Initializer Agent + Coding Agent)
- 원문의 실제 구조: 3단계 초기화 + 반복 세션
- Feature 추적은 Markdown보다 **JSON**이 안전 (모델이 덜 건드림)
- 가장 흔한 실패: "너무 일찍 완료 선언" → **self-verify 필수**

### 주의할 점

- 컨텍스트 오염은 눈에 보이지 않지만 코드 품질을 서서히 갉아먹는다
- 에이전트의 "구현 완료" 선언을 그대로 믿으면 안 된다. 검증 단계가 반드시 필요

### 더 파볼 거리

- [다음 글: 자율 에이전트 도구 실전 가이드 (Ralph Wiggum, Motlin 플러그인)](../claude-code-autonomous-agent-practice)
- 단일 범용 에이전트 vs 전문화된 다중 에이전트(테스트, QA, 코드 정리 역할 분리) 비교
- 웹 개발 외 도메인(데이터 분석, 인프라 관리)에서의 자율 에이전트 적용

## References

- [Effective harnesses for long-running agents - Anthropic Engineering](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
- [Ralph for Claude Code - GitHub](https://github.com/frankbria/ralph-claude-code)
- [Craig Motlin's Claude Code Plugins - GitHub](https://github.com/motlin/claude-code-plugins)
- [이전 글: Claude Code Skills 파헤치기](../claude-code-skills-deep-dive)
- [이전 글: 자바 개발자의 Claude Code 세팅](../claude-code-java-setup)
