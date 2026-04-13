# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Jekyll-based personal blog (hansw90.github.io) using the Minimal Mistakes remote theme, hosted on GitHub Pages. Content is written in Korean, covering Java, Spring, Elasticsearch, databases, NLP, and DevOps topics.

The actual site source lives in `hansw90.github.io/`.

## Build & Development Commands

```bash
cd hansw90.github.io

# Install dependencies
bundle install

# Local development server (http://localhost:4000)
bundle exec jekyll serve

# Production build
bundle exec jekyll build
```

Jekyll 3.9 with Kramdown (GFM) and Rouge syntax highlighting. `_config.yml` is NOT hot-reloaded — restart the server after editing it.

## Architecture

- **Theme**: Minimal Mistakes via `jekyll-remote-theme` (skin: "air"), heavily customized with a terminal/CLI dark theme in `assets/css/main.scss`
- **Content**: 127+ posts in `_posts/` organized by category subdirectories (java, elasticsearch, db, git, graphdb, nlp, others)
- **Navigation**: Defined in `_data/navigation.yml` — main nav and sidebar category tree
- **Comments**: Utterances (GitHub issue-based, `github-dark` theme)
- **Search**: Lunr.js with full content search enabled
- **Permalink**: `/:categories/:title/`
- **Pagination**: 10 posts per page

## Post Format

Posts go in `_posts/<category>/` with filename `YYYY-MM-DD-slug.md`:

```yaml
---
title: "[Category] Post Title"
date: YYYY-MM-DD HH:MM:SS +0900
categories: category_name
tags: [tag1, tag2]
excerpt: "Short description"
toc: true
toc_sticky: true
---
```

Default post values (set in `_config.yml`): layout `single`, author_profile, read_time, comments, share, related all enabled, sidebar nav set to "categories".

## Key Files

| File | Purpose |
|------|---------|
| `hansw90.github.io/_config.yml` | Main Jekyll config (site settings, defaults, plugins) |
| `hansw90.github.io/_data/navigation.yml` | Nav links and sidebar category structure |
| `hansw90.github.io/assets/css/main.scss` | Custom terminal-style dark theme overrides |
| `hansw90.github.io/Gemfile` | Ruby gem dependencies |

## Categories

Posts are organized by category subdirectory under `_posts/`:

| 디렉토리 | categories 값 | 내용 |
|----------|--------------|------|
| `_posts/ai-agent/` | `AI-AGENT` | Claude Code, AI 에이전트 도구, 자동화 |
| `_posts/java/` | `JAVA` | Java, Spring 관련 |
| `_posts/elasticsearch/` | `ELASTICSEARCH` | Elasticsearch |
| `_posts/db/` | `DB` | Database |
| `_posts/git/` | `GIT` | Git |
| `_posts/graphdb/` | `GRAPHDB` | Graph Database |
| `_posts/nlp/` | `NLP` | 자연어 처리 |
| `_posts/others/` | `OTHERS` | 기타 |

사이드바 네비게이션은 `_data/navigation.yml`에서 관리.

## 매일 아침 AI-Agent 뉴스 다이제스트

### 목적
매일 아침 AI-Agent 관련 최신 소식을 수집하여 사용자에게 전달한다.

### 뉴스 소스
`ai-agent-news-sources.md` (프로젝트 루트, git 미포함)에 전체 소스 목록이 있다. 우선순위 소스:
1. **Anthropic Blog / Engineering** — Claude 공식 업데이트
2. **Claude Code GitHub Issues/Discussions** — 커뮤니티 버그/기능 논의
3. **r/ClaudeAI** — 실사용 경험, 팁
4. **Hacker News** — AI/LLM/agent 키워드
5. **LangChain Blog, CrewAI Blog** — 에이전트 프레임워크 동향

### 전달 방식: Telegram 채널 플러그인
Claude Code의 Telegram 채널 플러그인(`telegram@claude-plugins-official`)을 사용한다.
이 채널은 양방향(two-way)으로, Claude가 reply 도구를 통해 Telegram으로 메시지를 보낼 수 있다.

#### 사전 준비
1. Telegram Bot 생성: @BotFather에서 봇 토큰 발급
2. Claude Code v2.1.80+ 필요, claude.ai 로그인 필요 (API 키 불가)

#### 설정 방법
```bash
# 1. Telegram 채널 플러그인 설치
/plugin install telegram

# 2. 채널 활성화하여 Claude Code 실행
claude --channels plugin:telegram@anthropic

# 3. 페어링: Telegram에서 봇에 DM → 코드 수신 → Claude Code 세션에서 승인
# 이후 양방향 통신 가능
```

#### Desktop Schedule + Telegram 채널 조합 (권장)
Desktop App에서 스케줄을 등록하면 로컬에서 실행되므로 Telegram 채널의 reply 도구 사용 가능.
```bash
# Desktop App Schedule 등록
# 평일 오전 8시 30분, Telegram 채널이 활성화된 상태에서
# ai-agent-news-sources.md 소스 기반으로 뉴스 수집 → 다이제스트 포맷 → Telegram reply로 전송
```

### 다이제스트 포맷
```
📌 AI-Agent 데일리 뉴스 (YYYY-MM-DD)

🔥 주요 소식
- [제목](링크) — 한줄 요약

🛠 Claude Code 업데이트
- [제목](링크) — 한줄 요약

💬 커뮤니티 핫토픽
- [제목](링크) — 한줄 요약

📦 GitHub 트렌딩
- [repo명](링크) — ⭐수, 한줄 설명
```

### 스케줄 설정 (준비되면 실행)
```bash
# Desktop Schedule + Telegram 채널 (권장)
# Desktop App에서 Telegram 채널 활성화 후 스케줄 등록:
/schedule 평일 오전 8시 30분에 ai-agent-news-sources.md 소스 목록 기반으로 AI-Agent 뉴스 다이제스트 만들어서 Telegram으로 보내줘
```

### 참고: 채널 디버깅
- 서버 로그: `~/.claude/debug/<session-id>.txt`
- MCP 상태 확인: 세션 내에서 `/mcp`
- 개발 중 테스트: `claude --dangerously-load-development-channels server:yourserver`

## 글 작성 톤 & 스타일 가이드

기존 포스트(`_posts/ai-agent/`)의 톤을 반드시 따를 것. 아래는 핵심 규칙이다.

### 구조 (필수 섹션 순서)

```
## 발단: [상황 설명]          ← 항상 "발단"으로 시작. 내가 겪은 상황/궁금증에서 출발
  → 일상적 상황 묘사 + 인용문(>) 형태의 내면 독백
  → 이전 글 링크로 자연스럽게 연결

## [본문 기술 섹션들]          ← 핵심 내용. 표, 코드블록, ASCII 다이어그램 적극 활용

## 정리                        ← 마무리 섹션
  ### TL;DR                   ← 불릿 포인트 요약 (5줄 이내)
  ### 주의할 점                ← 실수/함정 정리
  ### 더 파볼 거리             ← 후속 주제 링크

## References                 ← 원본 소스, 공식 문서, 관련 글 링크
```

### 톤 규칙

| 규칙 | 예시 |
|------|------|
| **1인칭 탐구 톤** | "직접 파헤쳐봤다", "뜯어보니 이랬다", "궁금해서 소스를 까봤다" |
| **해체/반말 혼용** | "~했다", "~인 것이다", "~인 셈이다" (논문체 아님, 개발자 일기체) |
| **내면 독백은 인용문** | `> "내가 자는 동안에도 Claude가 알아서 하면 안 되나?"` |
| **기술 용어는 영어 그대로** | Harness Engineering, Context Window, Progressive Disclosure |
| **비유로 설명** | "교대 근무하는 엔지니어들", "잘 정리된 매뉴얼" |
| **숫자/데이터 제시** | "PR 승인률 6.7% → 70%", "매주 1,300개 AI PR" |
| **허세/과장 금지** | "혁명적", "놀라운" 같은 표현 안 씀. 팩트와 수치로만 설득 |

### 제목 형식

```
title: "[카테고리] 핵심 주제 - 호기심 자극 부제"
```

예시:
- `"[AI-Agent] Claude Code Skills 파헤치기 - 프롬프트 복붙 지옥에서 탈출하는 법"`
- `"[AI-Agent] Claude Code Loop vs Schedule - 반복 자동화, 뭘 써야 할까?"`

### excerpt 형식

직접 해본 느낌 + 핵심 내용 요약. 1-2문장.

```
excerpt: "Claude Code의 /loop와 /schedule, 둘 다 반복 실행인데 뭐가 다를까? 세션 스코프 vs 클라우드 영구 실행, 내부 동작 원리부터 실전 사용 패턴까지 직접 파헤쳐봤다."
```

### 시각 요소 활용

- **표(table)**: 비교/정리에 적극 사용
- **ASCII 다이어그램**: 아키텍처, 흐름도에 사용 (Mermaid 아님)
- **코드블록**: 한국어 주석 포함, 실행 가능한 예시
- **인용문(>)**: 공식 문서 인용 또는 내면 독백

### YouTube 영상 기반 포스트 추가 규칙

- 영상을 그대로 번역/요약하지 말 것 — **내가 직접 파헤친 톤으로 재구성**
- "이 영상에서 Cole Medin이 말하길..." 식의 리포팅 금지
- 영상 내용 + WebSearch 보강 자료 + Claude 자체 지식을 종합해서 **독립적인 기술 글**로 작성
- 원본 영상은 References에만 링크
- 발단 섹션에서 "왜 이 주제가 궁금했는지" 자연스럽게 시작

## YouTube 기술 블로그 자동화

### 개요
영어 YouTube 기술 채널에서 최신 영상을 수집 → 자막 추출 → 관련 자료 보강 → 한국어 블로그 포스트 자동 생성

### 설정 파일
| 파일 | 용도 |
|------|------|
| `youtube-channels.yml` | 모니터링 대상 채널 목록 (priority 1/2/3) |
| `youtube-posted.json` | 이미 게시한 영상 ID (중복 방지) |

### 파이프라인 (스케줄 실행 시)

**Step 1: 새 영상 탐색**
```bash
# 채널별 최신 영상 3개씩 확인
yt-dlp --flat-playlist --playlist-items 1:3 -j "CHANNEL_URL/videos"
# youtube-posted.json에 없는 영상만 필터
```

**Step 2: 자막 추출**
```bash
# 자동자막 우선, 없으면 오디오 → Whisper STT fallback
yt-dlp --write-auto-sub --sub-lang en --sub-format srt --skip-download -o "/tmp/yt-blog/%(id)s" "VIDEO_URL"
```

**Step 3: 관련 자료 보강**
- 영상 주제 키워드로 WebSearch 추가 검색
- 같은 주제의 다른 영상/블로그/GitHub repo 수집
- 여러 소스를 교차 참조하여 내용 풍부하게 구성

**Step 4: 검증 & 글 작성**
- Claude 관련 내용 → Claude 자체 지식으로 팩트체크 (버전, API, 기능 정확성)
- 한국어 기술 블로그 톤으로 작성 (기존 _posts/ai-agent/ 글 스타일 참고)
- 원본 영상 링크 반드시 포함, 출처 명시

**Step 5: 발행**
```bash
# _posts/ai-agent/YYYY-MM-DD-slug.md 생성
# youtube-posted.json에 영상 ID 추가
# git add, commit, push
```

### 포스트 작성 가이드라인
- 영상을 그대로 번역하지 말 것 — 주제를 기반으로 재구성
- 여러 소스(영상 + 검색 결과)를 종합하여 깊이 있는 글 작성
- Claude/Anthropic 관련 기술 사실은 반드시 자체 지식으로 검증
- 코드 예제가 있으면 직접 검증 후 포함
- frontmatter 형식은 기존 ai-agent 포스트와 동일하게

### 스케줄 명령어
```bash
/schedule 평일 오전 9시에 youtube-channels.yml 채널 목록에서 새 영상 확인하고, 기술 블로그 포스트 자동 생성해서 발행해줘. youtube-posted.json으로 중복 체크하고, Claude 관련 내용은 팩트체크 필수.
```

## Commit Convention

Uses conventional commit prefixes: `docs:`, `fix:`, `style:`, `feat:`. Messages are a mix of Korean and English.
