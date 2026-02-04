---
title: "Claude Code Skills 파헤치기 - 프롬프트 복붙 지옥에서 탈출하는 법"
date: 2026-02-04 10:00:00 +0900
categories: OTHERS
tags: [claude, ai, skills, automation, productivity]
excerpt: "반복되는 프롬프트 입력에 지쳤다면, Skills가 답이다. 토큰도 아끼고, 일관성도 챙기는 마법 같은 구조를 파헤쳐 봤다."
toc: true
toc_sticky: true
---

## 발단: 프롬프트 복붙의 끝은 어디인가

Claude Code를 쓰다 보면 이런 상황이 반복된다.

> "이 코드 리뷰해줘. 근데 우리 팀 컨벤션은 이렇고, 에러 핸들링은 저렇게 해야 하고, 테스트는 꼭 포함하고..."

매번 같은 컨텍스트를 붙여넣는다. 귀찮다. 그리고 이게 토큰으로 환산되면 꽤 비싸다.

어느 날 문득 궁금해졌다. Claude가 "이 맥락을 기억해"라고 한 번만 알려주면 안 되나? 그래서 **Agent Skills**를 파보기 시작했다.

## Skills란 뭔가?

한 줄 요약:
> **"지시문, 스크립트, 리소스가 담긴 폴더를 Claude에게 통째로 넘겨주는 것"**

일반적인 프롬프트 엔지니어링과 다른 점은, Claude가 **필요할 때만** 이 정보를 꺼내 쓴다는 것이다. 마치 잘 정리된 매뉴얼 같다. 목차만 훑어보다가, 필요한 챕터만 펼쳐보는 식.

### 핵심 구조

모든 스킬의 중심에는 `SKILL.md` 파일이 있다.

```
my-skill/
├── SKILL.md           # 진입점 (필수)
├── reference.md       # 상세 레퍼런스 (선택)
├── examples.md        # 예제 모음 (선택)
└── scripts/
    └── helper.py      # 실행 가능한 스크립트 (선택)
```

`SKILL.md`는 두 부분으로 나뉜다:
1. **YAML frontmatter**: 스킬 메타데이터 (이름, 설명, 옵션)
2. **Markdown 본문**: Claude가 따를 지시문

```yaml
---
name: code-review
description: 팀 컨벤션에 맞춰 코드 리뷰를 수행한다
---

코드 리뷰 시 다음을 확인한다:
1. 에러 핸들링이 적절한가
2. 테스트가 포함되어 있는가
3. 네이밍 컨벤션을 따르는가
```

## 탐구: Progressive Disclosure의 마법

처음엔 "그냥 긴 시스템 프롬프트 아닌가?" 싶었다. 근데 아니었다.

### 3단계 점진적 노출

이게 Skills의 핵심 설계 철학이다.

| 단계 | 로드되는 내용 | 토큰 비용 |
|------|---------------|-----------|
| 1단계 | YAML 메타데이터만 (이름, 설명) | ~100 토큰 |
| 2단계 | SKILL.md 전체 본문 | < 5k 토큰 |
| 3단계 | 추가 파일들 (reference.md 등) | 필요할 때만 |

Claude는 1단계 정보만 가지고 "이 스킬이 지금 필요한가?"를 판단한다. 필요하다고 판단하면 그때서야 2단계를 읽는다.

```
Claude의 사고 과정:
"사용자가 코드 리뷰를 요청했네"
→ 스킬 목록 스캔 (1단계: ~100 토큰만 소비)
→ "code-review 스킬이 관련 있겠군"
→ 해당 SKILL.md 로드 (2단계)
→ 필요하면 reference.md도 로드 (3단계)
```

MCP와 비교하면 차이가 명확하다:
- **MCP**: 모든 도구/리소스를 처음부터 컨텍스트에 로드 → 2-3개 서버 연결하면 정확도 하락
- **Skills**: 필요한 것만 필요할 때 로드 → 수십 개 스킬도 문제없음

### 결정론적 결과

여기서 또 하나 놀란 점. 스킬 안에 Python 스크립트 같은 실행 코드를 포함할 수 있다.

```python
# scripts/extract_fields.py
import json
from PyPDF2 import PdfReader

def extract_form_fields(pdf_path):
    reader = PdfReader(pdf_path)
    # ... 폼 필드 추출 로직
    return fields
```

Claude는 이 스크립트를 직접 실행해서 결과를 가져온다. LLM의 비결정론적 특성을 우회하고, **항상 동일한 인풋에 동일한 아웃풋**을 보장할 수 있다.

이게 "AI 슬롭(Slop)"에서 벗어나는 핵심이었다. 스크립트가 정확한 작업을 처리하고, Claude는 그 결과를 해석하고 조합하는 역할만 한다.

## 원리: 실제 사용법

### 스킬 저장 위치

| 위치 | 경로 | 적용 범위 |
|------|------|-----------|
| 개인용 | `~/.claude/skills/<이름>/SKILL.md` | 모든 프로젝트 |
| 프로젝트용 | `.claude/skills/<이름>/SKILL.md` | 해당 프로젝트만 |
| 엔터프라이즈 | 관리 설정 | 조직 전체 |

### 실전 예시: 코드 설명 스킬

```yaml
---
name: explain-code
description: 코드를 시각적 다이어그램과 비유로 설명한다. "이게 어떻게 동작해?"라고 물을 때 사용.
---

코드 설명 시 항상 포함할 것:

1. **비유로 시작**: 일상생활의 무언가와 비교
2. **다이어그램 그리기**: ASCII 아트로 흐름/구조 시각화
3. **단계별 설명**: 실제로 무슨 일이 일어나는지
4. **함정 짚기**: 흔한 실수나 오해

복잡한 개념은 여러 비유를 사용한다.
```

사용법:
```bash
# Claude가 자동으로 호출하게 하려면
> 이 코드 어떻게 동작해?

# 직접 호출하려면
> /explain-code src/auth/login.ts
```

### 인자 전달하기

`$ARGUMENTS` 플레이스홀더를 사용하면 유연하게 인자를 받을 수 있다.

```yaml
---
name: fix-issue
description: GitHub 이슈를 수정한다
disable-model-invocation: true  # 수동으로만 호출
---

GitHub 이슈 $ARGUMENTS 수정 절차:

1. 이슈 내용 읽기
2. 요구사항 파악
3. 수정 구현
4. 테스트 작성
5. 커밋 생성
```

```bash
> /fix-issue 123
# Claude는 "GitHub 이슈 123 수정 절차..."를 받게 됨
```

개별 인자 접근도 가능하다:
```yaml
$ARGUMENTS[0]  # 또는 $0
$ARGUMENTS[1]  # 또는 $1
```

### 동적 컨텍스트 주입

이게 진짜 강력하다. 쉘 명령어 결과를 스킬에 주입할 수 있다.

```yaml
---
name: pr-summary
description: PR을 요약한다
context: fork
allowed-tools: Bash(gh *)
---

## PR 컨텍스트
- PR diff: !`gh pr diff`
- PR 코멘트: !`gh pr view --comments`
- 변경된 파일: !`gh pr diff --name-only`

## 할 일
이 PR을 요약해라...
```

`!`command`` 문법으로 실시간 데이터를 가져와서 프롬프트에 삽입한다.

### 호출 제어하기

| frontmatter 설정 | 사용자 호출 | Claude 호출 |
|------------------|-------------|-------------|
| (기본값) | O | O |
| `disable-model-invocation: true` | O | X |
| `user-invocable: false` | X | O |

배포 같은 위험한 작업은 `disable-model-invocation: true`로 설정해서 사용자만 호출하게 한다.

```yaml
---
name: deploy
description: 프로덕션 배포
disable-model-invocation: true  # Claude가 멋대로 배포하는 건 막자
---
```

## 스킬 vs 플러그인

자주 헷갈리는 개념이다.

| 개념 | 정의 |
|------|------|
| **Skill** | 특정 작업을 위한 지시문 + 리소스 묶음 |
| **Plugin** | 스킬 + 커스텀 명령어 + 서브에이전트 + MCP 서버를 포함하는 종합 패키지 |

플러그인이 더 상위 개념이다. 하나의 플러그인 안에 여러 스킬이 들어갈 수 있다.

## 정리: 미래의 나에게

### TL;DR

- **스킬이란**: SKILL.md + 리소스 폴더 = Claude에게 전문 지식 부여
- **핵심 원리**: Progressive Disclosure로 토큰 효율화
- **저장 위치**: `~/.claude/skills/` (개인) 또는 `.claude/skills/` (프로젝트)
- **호출 방법**: `/skill-name` 또는 Claude가 자동 판단

### 언제 스킬을 만들어야 하나

- 같은 프롬프트를 3번 이상 복붙하고 있다면
- 팀 컨벤션이나 특정 워크플로우를 표준화하고 싶다면
- 스크립트 실행 결과를 Claude와 함께 처리하고 싶다면

### 주의할 점

- SKILL.md는 **500줄 미만**으로 유지 (넘으면 파일 분할)
- 설명(description)을 잘 작성해야 Claude가 적절히 호출함
- 민감한 작업은 `disable-model-invocation: true` 필수

### 더 파볼 거리

- [Subagents](https://code.claude.com/docs/en/sub-agents): 스킬을 서브에이전트에서 실행
- [Hooks](https://code.claude.com/docs/en/hooks): 스킬 라이프사이클에 자동화 연결
- [Plugins](https://code.claude.com/docs/en/plugins): 스킬을 포함한 종합 패키지 배포

## References

- [Extend Claude with skills - Claude Code 공식 문서](https://code.claude.com/docs/en/skills)
- [Equipping agents for the real world with Agent Skills - Anthropic 블로그](https://claude.com/blog/equipping-agents-for-the-real-world-with-agent-skills)
- [Agent Skills - Claude API 문서](https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview)
- [How Anthropic's Skills make Claude faster and cheaper - VentureBeat](https://venturebeat.com/ai/how-anthropics-skills-make-claude-faster-cheaper-and-more-consistent-for)
- [SkillsMP - Agent Skills Marketplace](https://skillsmp.com/)
