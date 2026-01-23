---
title: "Claude Code 시작하기 - 터미널에서 AI와 함께 코딩하기"
date: 2026-01-24 09:00:00 -0000
categories: OTHERS
tags: [claude, ai, coding, cli, productivity]
excerpt: "터미널을 떠나지 않고 AI와 협업하는 방법, Claude Code를 파헤쳐 봤다"
toc: true
toc_sticky: true
---

## 발단: 또 다른 AI 코딩 도구?

요즘 AI 코딩 도구가 쏟아져 나온다. GitHub Copilot, Cursor, Codeium... 하나같이 IDE에 플러그인 형태로 붙거나, 아예 새로운 IDE를 설치해야 한다.

그런데 나는 터미널을 좋아한다. vim으로 코드 짜고, tmux로 화면 분할하고, zsh 플러그인으로 개발 환경을 꾸며놨다. "또 새로운 IDE 깔아야 해?"라는 생각에 약간 피로감이 들던 차에, **Claude Code**라는 녀석을 발견했다.

터미널에서 돌아간다고? 한번 파봐야겠다.

## Claude Code가 뭔데?

Anthropic에서 만든 **에이전트 코딩 도구**다. 핵심 특징은 이렇다:

- **터미널 네이티브**: IDE 플러그인이 아니라 CLI 도구
- **에이전트 방식**: 단순 자동완성이 아니라, 직접 파일을 수정하고 명령을 실행
- **Unix 철학**: 파이프로 연결하고, 스크립트로 자동화 가능

처음엔 "터미널에서 AI가 뭘 할 수 있겠어?"라고 생각했는데, 실제로 써보니 생각이 달라졌다.

## 설치: 30초면 끝

### macOS/Linux

```bash
curl -fsSL https://claude.ai/install.sh | bash
```

### Homebrew (macOS)

```bash
brew install --cask claude-code
```

### Windows PowerShell

```powershell
irm https://claude.ai/install.ps1 | iex
```

설치 후 프로젝트 디렉토리에서 `claude`만 치면 된다.

```bash
cd your-project
claude
```

처음 실행하면 로그인하라고 뜬다. [Claude.ai](https://claude.ai) 계정이나 [Console](https://console.anthropic.com/) 계정으로 인증하면 끝.

> Native 설치 방식은 백그라운드에서 자동 업데이트된다. Homebrew는 `brew upgrade claude-code`로 수동 업데이트해야 한다.

## 이게 되네? 핵심 기능들

### 1. 코드베이스 탐색

새 프로젝트에 투입됐을 때 제일 먼저 하는 일이 뭔가? 폴더 구조 파악하고, 주요 파일 찾고... Claude Code에게 그냥 물어보면 된다.

```
> give me an overview of this codebase
```

전체 구조와 아키텍처 패턴을 분석해서 설명해준다.

```
> find the files that handle user authentication
```

관련 파일을 찾아서 실행 흐름까지 추적해준다.

### 2. 버그 수정

스택 트레이스를 그대로 붙여넣으면:

```
> [스택 트레이스 붙여넣기]
> fix this bug
```

코드베이스를 분석해서 문제를 찾고, 수정까지 제안한다. 그리고 **직접 파일을 수정**할 수 있다. 단순 제안이 아니라 실제 액션을 취한다는 점이 다른 도구와의 차이점이다.

### 3. 리팩토링

```
> refactor this file to use ES2024 patterns
```

레거시 코드를 최신 패턴으로 업데이트해준다. 변경 후에는 테스트 실행까지.

### 4. PR 생성

```
> create a pr
```

변경 사항을 분석해서 상세한 설명이 포함된 PR을 자동 생성한다.

## 진짜 좋은 점: Unix 철학

이게 제일 마음에 들었다. 파이프 연결이 된다.

```bash
cat build-error.txt | claude -p 'explain'
```

```bash
tail -f app.log | claude -p "Slack me if you see any anomalies"
```

CI에서도 쓸 수 있다:

```bash
claude -p "If there are new text strings, translate them into French and raise a PR"
```

스크립트로 자동화가 가능하니까 활용 범위가 엄청 넓어진다.

## Plan Mode: 신중하게 접근하기

복잡한 작업을 할 때는 바로 코드를 수정하는 게 부담스러울 수 있다. Plan Mode는 '읽기 전용' 모드로, 분석과 계획만 세운다.

```bash
claude --permission-mode plan
```

또는 세션 중에 `Shift+Tab`으로 전환 가능.

다단계 구현이나 복잡한 리팩토링(예: OAuth2 도입) 계획을 세울 때 유용하다. 계획이 마음에 들면 그때 실행 모드로 전환하면 된다.

## 서브에이전트(Subagents)

특정 작업에 최적화된 AI 에이전트를 호출할 수 있다.

```
> /agents
```

보안 리뷰, 테스트 실행 등 작업 성격에 맞춰 자동으로 적절한 서브에이전트에게 업무를 위임한다.

프로젝트 전용 서브에이전트도 만들 수 있다. `.claude/agents/` 디렉토리에 정의해두면 팀과 공유 가능.

## 확장 사고(Thinking Mode)

복잡한 추론이 필요할 때 Claude가 단계별로 사고하도록 예산을 할당할 수 있다.

- Sonnet 4.5/Opus 4.5는 기본 활성화
- 다른 모델은 `/config`나 `ultrathink` 키워드로 활성화

최대 31,999 토큰까지 내부 추론에 사용 가능. `Ctrl+O`를 누르면 Claude의 사고 프로세스를 볼 수 있다.

## 세션 관리

```bash
# 마지막 대화 이어가기
claude --continue

# 이전 세션 목록에서 선택
claude --resume
```

Git Worktree와 함께 쓰면 여러 작업을 병렬로 처리할 수 있다. 각 worktree에서 별도의 Claude 세션을 운영하면 완전한 코드 격리 상태로 작업 가능.

## 커스텀 명령어

자주 쓰는 프롬프트를 명령어로 만들 수 있다.

```
# 프로젝트 전용
.claude/commands/optimize.md

# 개인용
~/.claude/commands/optimize.md
```

`/optimize` 같은 자신만의 슬래시 명령어를 정의해서 사용. `$ARGUMENTS`로 유연한 입력도 가능.

## 정리: 미래의 나에게

### TL;DR

- **설치**: `curl -fsSL https://claude.ai/install.sh | bash`
- **실행**: 프로젝트 폴더에서 `claude`
- **핵심**: 터미널 네이티브 + 에이전트(직접 액션) + Unix 파이프 지원
- **Plan Mode**: `Shift+Tab` 또는 `--permission-mode plan`
- **세션 이어가기**: `--continue` 또는 `--resume`

### 주의할 점

- 파일을 직접 수정할 수 있으니 중요한 작업 전에는 git commit 해두자
- Plan Mode로 먼저 계획을 세우고 검토하는 습관
- 자동 업데이트는 Native 설치만 지원, Homebrew/WinGet은 수동

### 더 파볼 거리

- MCP(Model Context Protocol)로 외부 도구 연동 (Google Drive, Jira, Slack 등)
- Agent SDK로 커스텀 에이전트 만들기
- CI/CD 파이프라인에 통합하기

## References

- [Claude Code 공식 문서](https://code.claude.com/docs)
- [Claude Code 제품 페이지](https://claude.com/product/claude-code)
- [Agent SDK 문서](https://docs.claude.com/en/docs/agent-sdk/overview)
- [GitHub - Claude Code DevContainer](https://github.com/anthropics/claude-code/tree/main/.devcontainer)
