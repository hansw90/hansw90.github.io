---
title: "[AI-Agent] Harness Engineering — 프롬프트 장인 시대는 끝났다"
date: 2026-04-14 09:00:00 +0900
categories: AI-AGENT
tags: [ai-agent, harness-engineering, archon, claude-code, ai-coding, workflow, context-engineering]
excerpt: "Prompt Engineering → Context Engineering → Harness Engineering. AI 코딩 에이전트 하나로는 한계가 있다. 여러 세션을 오케스트레이션하는 하네스를 직접 만들 수 있는 시대가 됐다. Archon을 뜯어봤다."
toc: true
toc_sticky: true
---

## 발단: "왜 매번 처음부터야?"

[이전 글](../claude-code-loop-vs-schedule)에서 `/loop`와 `/schedule`로 반복 자동화를 구축했다. 꽤 만족스러웠다.

그런데 실제로 큰 기능을 맡기면 여전히 이런 일이 벌어진다.

> "계획하고, 구현하고, 테스트하고, 리뷰해서 PR 올려줘"

Claude Code가 열심히 하긴 한다. 근데 중간에 테스트를 빼먹거나, 코드 리뷰 없이 바로 PR을 올린다. 같은 프롬프트인데 어제와 오늘 결과가 다르다. 확률적 모델이니 당연하다.

문제는 **모델이 멍청해서가 아니라, 프로세스가 없어서**였다.

그러다 Archon이라는 프로젝트를 발견했다. AI 코딩 에이전트를 감싸는 **하네스(Harness)**를 직접 빌드할 수 있게 해주는 오픈소스 도구다. 뜯어봤다.

## 진화의 흐름: Prompt → Context → Harness

| 시기 | 패러다임 | 핵심 질문 |
|------|---------|----------|
| 2022-2024 | **Prompt Engineering** | LLM에게 최적의 출력을 뽑는 프롬프트는? |
| 2025 | **Context Engineering** | 에이전트에 필요한 맥락만 정확히 넣으려면? |
| 2026~ | **Harness Engineering** | 여러 에이전트 세션을 어떻게 엮을 것인가? |

Prompt Engineering은 단일 LLM 호출 최적화였다. Context Engineering은 `CLAUDE.md`나 RAG로 에이전트에게 적절한 맥락을 주입하는 것이었다.

**Harness Engineering은 한 단계 위다.** 여러 코딩 에이전트 세션을 체계적으로 연결하고, 결정적(deterministic) 단계를 강제한다.

[이전 글의 자율 에이전트 개념편](../claude-code-autonomous-agent-concept)에서 다룬 "2-파트 하네스 아키텍처"를 기억하는가? Anthropic이 제안한 Initializer Agent + Coding Agent 구조. 그 아이디어를 **범용 도구로 만든 것**이 Archon이다.

## 숫자가 말해준다

하네스의 효과는 이미 검증됐다.

- **하네스 없이** LLM이 생성한 코드의 PR 승인률: **6.7%**
- **하네스 적용 시**: **최대 ~70%**
- **Stripe Minion**: 하네스 기반으로 매주 **1,300개 AI 전용 PR**을 프로덕션에 머지

Stripe는 자체 하네스를 만들어서 이 성과를 냈다. 근데 당연히 비공개다. 우리가 쓸 수 없다.

> 한편, Anthropic도 하네스에 올인하고 있다. Claude Code 코드베이스 중 약 40%가 하네스 관련 코드라는 분석이 나왔다 — 서브 에이전트, 에이전트 팀 기능 등이 여기 해당한다.

비싼 모델을 쓸 수 없다면? 저렴한 모델을 하네스로 감싸서 비싼 모델보다 나은 결과를 만들면 된다. 이건 실험이 아니라 이미 프로덕션에서 증명된 패턴이다.

## Archon: 오픈소스 하네스 빌더

[Archon](https://github.com/coleam00/Archon)은 Cole Medin이 만든 오픈소스 프로젝트다. 핵심 아이디어는 간단하다.

> **개발 프로세스를 YAML 워크플로우로 정의하고, 그 위에서 코딩 에이전트를 돌린다.**

### 두 종류의 노드

워크플로우는 **노드의 체인**이다. 노드는 딱 두 종류:

**프롬프트 노드** — AI의 판단이 필요한 작업. 계획 수립, 구현, 코드 리뷰.

**커맨드 노드** — 반드시 실행되어야 하는 결정적 작업. 테스트 실행, 컨텍스트 주입, 검증. AI가 "이번엔 테스트 안 돌려도 될 것 같아요"라고 건너뛰는 걸 **원천 차단**한다.

### 워크플로우 예시

```
[Plan]          [Implement]         [Test]         [Review]      [PR]
 프롬프트 노드 → 프롬프트 노드 → 커맨드 노드 → 프롬프트 노드 → 커맨드 노드
 (새 세션)       (새 세션)        (강제 실행)     (새 세션)      (강제 실행)
                                     │
                                     ├── PASS → 다음
                                     └── FAIL → Implement로 되돌아감
```

YAML로 표현하면 이런 구조다:

```yaml
nodes:
  - name: plan
    type: prompt
    session: new          # 새 컨텍스트에서 계획
    prompt: "이슈를 분석하고 구현 계획 작성"

  - name: implement
    type: prompt
    session: new          # 계획과 별도 세션 (편향 제거)
    context: [plan.output]

  - name: test
    type: command         # 결정적 실행 — AI 판단 아님
    run: "npm test"
    on_fail: retry(implement)

  - name: review
    type: prompt
    session: new

  - name: human_approval
    type: gate            # 사람 확인

  - name: create_pr
    type: command
    run: "gh pr create"
```

여기서 핵심 설계 결정 세 가지:

1. **계획과 구현은 별도 세션이다.** 같은 세션에서 하면 계획 단계의 사고에 구현이 끌려간다. [자율 에이전트 편](../claude-code-autonomous-agent-concept)에서 다룬 "컨텍스트 오염"과 같은 맥락이다.

2. **테스트는 커맨드 노드로 강제한다.** 에이전트가 "건너뛸" 선택지 자체가 없다.

3. **Human Approval Gate로 사람을 워크플로우에 끼울 수 있다.** 완전 자율이 아니라 반자율.

## 하이브리드 시크릿

Stripe Minion이 매주 1,300개 PR을 머지할 수 있는 비밀이 여기 있다.

워크플로우의 모든 단계를 AI에게 맡기는 게 아니다. **AI가 잘하는 것과 결정적으로 실행해야 하는 것을 분리**한다.

```
AI가 잘하는 것              결정적으로 해야 하는 것
─────────────────          ─────────────────────
계획 수립                    테스트 실행
코드 구현                    린트/타입체크
코드 리뷰                    컨텍스트 큐레이션
버그 원인 분석               배포 스크립트
```

Archon에서는 노드별로 스킬과 MCP 서버를 다르게 주입할 수 있다. 계획 단계에서는 MCP로 이슈 트래커를 연결하고, 검증 단계에서는 테스트 스킬만 주입하는 식이다.

## Claude Code와의 관계

이미 Claude Code를 쓰고 있다면, 하네스 개념의 조각들을 이미 알고 있는 셈이다.

| 이미 쓰고 있는 것 | 하네스에서의 역할 |
|------------------|----------------|
| `CLAUDE.md` | 컨텍스트 엔지니어링 (노드별로 다르게 주입 가능) |
| Skills / 슬래시 커맨드 | 프롬프트 노드의 재사용 단위 |
| Sub-agents | 병렬 노드 실행 |
| Hooks | 커맨드 노드와 동일한 결정적 실행 |
| `/loop`, `/schedule` | 워크플로우 트리거 |

Archon은 이걸 **YAML 하나로 묶어서 재현 가능한 파이프라인**으로 만든다. 프로젝트마다 Skills 세팅을 다시 하는 대신, 워크플로우를 정의하고 어디서든 돌리는 것이다.

17개의 기본 워크플로우가 제공된다 — GitHub 이슈 수정, PR 생성, PR 검증/리뷰, PRD 작성(Human-in-the-loop) 등.

## 정리

### TL;DR

- **Harness Engineering**: 여러 코딩 에이전트 세션을 YAML 워크플로우로 오케스트레이션
- **핵심 원리**: 프롬프트 노드(AI 판단) + 커맨드 노드(결정적 실행) 분리
- **Archon**: 오픈소스 하네스 빌더. 17개 기본 워크플로우 + 커스텀 빌드 가능
- **효과**: PR 승인률 6.7% → ~70%. Stripe는 매주 1,300개 AI PR을 이걸로 돌린다
- **Claude Code 사용자**: Skills, Hooks, Sub-agents를 이미 쓰고 있다면 하네스의 조각을 알고 있는 것

### 주의할 점

- 하네스 최적화에 시간을 쏟느라 정작 코드를 안 짜는 함정이 있다. 일단 기본 워크플로우로 시작하고, 필요할 때 커스텀하는 게 맞다
- 하네스가 만능은 아니다. 탐색적 작업(프로토타이핑, 디버깅)은 단일 세션이 더 빠를 수 있다
- 워크플로우를 너무 복잡하게 만들면 디버깅이 오히려 어려워진다

### 더 파볼 거리

- [Archon GitHub](https://github.com/coleam00/Archon)에서 기본 워크플로우 17개 직접 돌려보기
- Archon + Claude Code `/schedule` 조합으로 야간 자동 코딩 파이프라인 구축
- [12 Agentic Harness Patterns from Claude Code](https://medium.com/@simranjeetsingh1497/agent-harness-12-agentic-harness-patterns-from-claude-code-5505b7c239c4) — 실전 하네스 패턴 정리

## References

- [Archon - GitHub](https://github.com/coleam00/Archon)
- [Harness Engineering Explained - AI Code Invest](https://aicodeinvest.com/harness-engineering-claude-code-ai-agents-guide/)
- [Claude Code Agent Harness: Architecture Breakdown - WaveSpeedAI](https://wavespeed.ai/blog/posts/claude-code-agent-harness-architecture/)
- [Effective harnesses for long-running agents - Anthropic Engineering](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
- [Best Practices for Claude Code - 공식 문서](https://code.claude.com/docs/en/best-practices)
- [이전 글: Loop vs Schedule](../claude-code-loop-vs-schedule)
- [이전 글: 자율 에이전트 개념편](../claude-code-autonomous-agent-concept)
