---
title: "자바 개발자의 Claude Code 세팅 - 스킬로 나만의 AI 페어 프로그래머 만들기"
date: 2026-02-06 10:00:00 +0900
categories: AI-AGENT
tags: [claude, ai, java, spring-boot, skills, developer-tools, productivity]
excerpt: "Claude Code에 자바 DNA를 심어보자. Skills 구조를 활용해 코드 리뷰부터 Spring Boot 패턴까지, 자바 생태계에 최적화된 AI 개발 환경을 구축한다."
toc: true
toc_sticky: true
---

## 발단: "자바 프로젝트에서 Claude가 좀 아쉬운데..."

[지난 글](../claude-code-skills-deep-dive)에서 Skills의 구조와 원리를 파헤쳤다. Progressive Disclosure로 토큰을 아끼고, `SKILL.md`에 전문 지식을 담아 Claude를 특화시키는 방법을 알아봤다.

그런데 실제로 쓰다 보니 한 가지 불만이 생겼다.

> "Claude야, 이 서비스 레이어 리뷰해줘."
>
> Claude: *일반적인 코드 리뷰 수행*
>
> "아니, `@Transactional` 전파 속성 확인했어? 같은 클래스 내부 호출이면 프록시 안 타잖아."

매번 자바 특유의 함정들을 일일이 짚어줘야 했다. Spring의 프록시 메커니즘, JPA의 N+1 문제, `Optional`의 올바른 사용법... Claude는 똑똑하지만, **자바 시니어 개발자의 코드 리뷰 체크리스트**가 머릿속에 없다.

그래서 생각했다. Skills로 Claude에게 자바 DNA를 심으면 어떨까?

마침 GitHub에서 정확히 이 문제를 해결한 프로젝트를 발견했다.

## claude-code-java: 자바 전용 AI 인프라

[decebals/claude-code-java](https://github.com/decebals/claude-code-java)는 PF4J(자바 플러그인 프레임워크) 개발자 Decebal Suiu가 만든 프로젝트다. 자바 라이브러리가 아니다. **Claude Code에 자바 전문 지식을 주입하는 스킬 세트 + 자동화 인프라**다.

구조를 보면 의도가 명확하다:

```
claude-code-java/
├── .claude/skills/           # 18개의 자바 전용 스킬 (핵심)
├── scripts/                  # 프로젝트 셋업 자동화
│   ├── setup-project.sh      # 오케스트레이터
│   ├── link-skills.sh        # 스킬 심볼릭 링크
│   ├── generate-claude-md.sh # CLAUDE.md 생성
│   └── configure-mcp.sh      # MCP 서버 설정
├── templates/                # 설정 파일 템플릿
│   ├── CLAUDE.md.template
│   ├── settings.json.template
│   └── mcp-config.json.template
└── docs/                     # 설계 원칙, 안전 가이드
```

18개 스킬이 다 자바/Spring 생태계에 특화되어 있다. 이걸 하나씩 까보자.

## 탐구: 18개 스킬 해부하기

### 스킬 카테고리 전체 지도

스킬들을 카테고리별로 정리하면 이렇다:

| 카테고리 | 스킬 | 하는 일 |
|---------|------|--------|
| **코드 품질** | java-code-review | Null Safety, 동시성, 리소스 관리 등 9개 카테고리 체크 |
| | api-contract-review | REST API 설계 검증 (HTTP 동사, 상태 코드, 버전관리) |
| | concurrency-review | Virtual Thread, `@Async` 함정, CompletableFuture 패턴 |
| | performance-smell-detection | 성능 안티패턴 탐지 (근데 "측정 먼저" 철학) |
| | security-audit | OWASP Top 10 기반 보안 검사 |
| | test-quality | JUnit 5 + AssertJ + Mockito 베스트 프랙티스 |
| **프레임워크** | spring-boot-patterns | Controller/Service/Repository/DTO/Exception 전체 스택 |
| | jpa-patterns | N+1, 지연 로딩, 트랜잭션 관리, 낙관적 락 |
| | logging-patterns | 구조화된 JSON 로깅, MDC, AI 친화적 로그 |
| **아키텍처** | architecture-review | 패키지 구조, 의존성 방향, 아키텍처 냄새 |
| | solid-principles | SOLID 각 원칙별 위반/개선 코드 예시 |
| | design-patterns | Builder, Strategy, Observer 등 GoF 패턴 |
| | clean-code | DRY/KISS/YAGNI + 자바 네이밍 규칙 |
| **워크플로우** | git-commit | Conventional Commits + 자바 스코프 |
| | changelog-generator | git 히스토리에서 CHANGELOG 자동 생성 |
| | issue-triage | GitHub 이슈 배치 분류 |
| **마이그레이션** | java-migration | Java 8→11→17→21→25 + Spring Boot 2→3 |
| | maven-dependency-audit | 의존성 업데이트, 충돌 검사, 보안 스캔 |

18개. 자바 개발의 거의 모든 영역을 커버한다.

### 핵심 스킬 깊게 들여다보기

#### 1. java-code-review: 자바 시니어의 눈

이 스킬이 제일 인상 깊었다. 9개 카테고리로 체계적인 리뷰 체크리스트를 제공한다:

```markdown
## 리뷰 체크리스트

1. **Null Safety**: Optional, @Nullable, 빈 컬렉션 반환
2. **Exception Handling**: 빈 catch, 넓은 catch, 스택 트레이스 유실
3. **Collections & Streams**: ConcurrentModificationException, 스트림 오용
4. **Concurrency**: 공유 가변 상태, check-then-act, volatile
5. **Java Idioms**: equals/hashCode, toString, 빌더, 패턴 매칭
6. **Resource Management**: try-with-resources
7. **API Design**: boolean 파라미터, null 핸들링, 검증
8. **Performance**: 문자열 결합, 루프 내 정규식, N+1
9. **Testing hints**: 테스트 관련 제안
```

예를 들어 이런 코드를 리뷰에 넘기면:

```java
public class UserService {

    public User findUser(String id) {
        try {
            User user = userRepository.findById(id);
            if (user == null) {
                return null;  // 🚩 null 반환
            }
            return user;
        } catch (Exception e) {  // 🚩 넓은 catch
            System.out.println("error: " + e);  // 🚩 스택 트레이스 유실
            return null;
        }
    }
}
```

스킬이 로드된 Claude는 단순히 "null을 반환하지 마세요"가 아니라 이렇게 잡아낸다:

```java
// Claude의 리뷰 결과 (스킬 적용 후)
public class UserService {

    public Optional<User> findUser(String id) {  // Optional 반환
        try {
            return userRepository.findById(id);  // Spring Data는 이미 Optional 지원
        } catch (DataAccessException e) {  // 구체적인 예외
            log.error("사용자 조회 실패: id={}", id, e);  // 스택 트레이스 보존 + 구조화된 로깅
            throw new UserNotFoundException(id, e);  // 의미 있는 예외로 변환
        }
    }
}
```

3가지 문제를 동시에 잡는다. 이게 시니어가 보는 코드 리뷰다.

#### 2. spring-boot-patterns: Spring 풀스택 가이드

이 스킬은 Spring Boot 프로젝트의 전체 레이어를 다룬다. 내가 특히 좋았던 부분은 **DTO 패턴**이다:

```java
// Java Record를 활용한 DTO (Java 16+)
public record CreateUserRequest(
    @NotBlank(message = "이름은 필수입니다")
    String name,

    @Email(message = "유효한 이메일을 입력하세요")
    String email,

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    String password
) {}

public record UserResponse(
    Long id,
    String name,
    String email,
    LocalDateTime createdAt
) {
    // Entity → DTO 변환 팩토리 메서드
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}
```

그리고 `@RestControllerAdvice`를 활용한 전역 예외 처리 패턴:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_FAILED", message));
    }
}
```

이런 패턴들을 Claude가 기본 탑재하고 있으면, 새 API를 만들 때 일일이 설명할 필요가 없다.

#### 3. jpa-patterns: N+1 문제의 3가지 해결책

자바 개발자라면 한 번쯤 겪어봤을 N+1 문제. 이 스킬은 해결책 3가지를 명확히 구분한다:

```java
// 🚩 N+1 문제 발생 코드
@Entity
public class Team {
    @OneToMany(mappedBy = "team")
    private List<Member> members;  // 팀 조회 후 멤버마다 추가 쿼리 발생
}

// 해결책 1: JOIN FETCH (JPQL)
@Query("SELECT t FROM Team t JOIN FETCH t.members WHERE t.id = :id")
Optional<Team> findByIdWithMembers(@Param("id") Long id);

// 해결책 2: @EntityGraph (선언적)
@EntityGraph(attributePaths = {"members"})
Optional<Team> findById(Long id);

// 해결책 3: @BatchSize (배치 로딩)
@Entity
public class Team {
    @OneToMany(mappedBy = "team")
    @BatchSize(size = 100)  // IN 절로 배치 조회
    private List<Member> members;
}
```

각각의 트레이드오프까지 Claude가 설명해준다. JOIN FETCH는 페이징이 안 되고, @EntityGraph는 카테시안 곱 위험이 있고, @BatchSize는 쿼리 수를 줄여주지만 제거하진 못한다.

#### 4. concurrency-review: Java 21 시대의 동시성

이 스킬이 특히 **시의적절**하다고 느꼈다. Java 21의 Virtual Thread와 기존 코드의 충돌 포인트를 짚어준다:

```java
// 🚩 ThreadLocal + Virtual Thread = 위험
private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

// ✅ Java 21+: ScopedValue 사용
private static final ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();

ScopedValue.runWhere(CURRENT_USER, user, () -> {
    // 이 블록 내에서 CURRENT_USER.get()으로 접근
    processRequest();
});
```

그리고 Spring `@Async`의 5가지 함정:

| 함정 | 원인 |
|------|------|
| `@Async`가 안 먹힘 | `@EnableAsync` 안 붙임 |
| 같은 클래스 내부 호출 | 프록시를 안 탐 (= `@Transactional`과 같은 문제) |
| private 메서드 | 프록시가 오버라이드 못 함 |
| 기본 executor | `SimpleAsyncTaskExecutor`는 쓰레드 풀이 아님 |
| SecurityContext 유실 | 다른 쓰레드로 전파 안 됨 |

이런 걸 Claude가 코드 리뷰 때 자동으로 체크해준다면? 상당히 강력하다.

## 원리: 실제로 세팅하는 법

### 방법 1: claude-code-java 직접 사용

가장 빠른 방법이다.

```bash
# 1. 레포 클론
git clone https://github.com/decebals/claude-code-java.git ~/claude-code-java

# 2. 내 프로젝트에 셋업
cd ~/my-spring-project
~/claude-code-java/scripts/setup-project.sh .
```

이 한 줄이 4가지를 한다:

1. **link-skills.sh**: `.claude/skills/`에 심볼릭 링크 생성 → 18개 스킬 연결
2. **generate-claude-md.sh**: 프로젝트용 `CLAUDE.md` 템플릿 생성
3. **configure-mcp.sh**: GitHub/Filesystem/Git MCP 서버 설정
4. **configure-settings.sh**: Maven/Gradle 명령어 자동 승인 설정

핵심은 **심볼릭 링크** 전략이다:

```bash
# 스킬을 복사하는 게 아니라 링크한다
ln -sf ~/claude-code-java/.claude/skills .claude/skills

# 결과: claude-code-java를 업데이트하면 모든 프로젝트가 최신 스킬을 사용
```

그리고 `settings.json`에서 안전한 명령어를 미리 승인해둔다:

```json
{
  "permissions": {
    "allow": [
      "Bash(mvn *)",
      "Bash(./mvnw *)",
      "Bash(gradle *)",
      "Bash(./gradlew *)",
      "Bash(git status)",
      "Bash(git log *)",
      "Bash(git diff *)"
    ]
  }
}
```

`git commit`이나 `git push`는 **의도적으로 빠져있다**. "AI가 멋대로 커밋하는 건 안 된다"는 설계 철학. 동의한다.

### 방법 2: 필요한 스킬만 직접 만들기

18개가 전부 필요하진 않을 수 있다. 내 경우를 예로 들면, 실제로 매일 쓸 만한 스킬을 직접 만들어보겠다.

#### 예제: Spring Boot 코드 리뷰 스킬

```bash
mkdir -p .claude/skills/spring-review
```

```yaml
# .claude/skills/spring-review/SKILL.md
---
name: spring-review
description: Spring Boot 프로젝트 코드 리뷰. "리뷰해줘", "이 코드 봐줘"라고 하면 자동 실행.
---

## Spring Boot 코드 리뷰 체크리스트

아래 항목을 순서대로 검사하고, 발견된 문제만 보고한다.

### 1. 트랜잭션
- [ ] `@Transactional`이 같은 클래스 내부 호출에서 사용되고 있지 않은가?
- [ ] readOnly 트랜잭션이 적절히 분리되어 있는가?
- [ ] 트랜잭션 범위가 최소화되어 있는가? (외부 API 호출이 트랜잭션 안에 있지 않은가?)

### 2. JPA/Hibernate
- [ ] N+1 쿼리 위험이 있는 연관관계가 있는가?
- [ ] `@OneToMany` 기본 fetch = LAZY가 설정되어 있는가?
- [ ] 엔티티를 API 응답으로 직접 반환하고 있지 않은가? (DTO 사용)

### 3. API 설계
- [ ] HTTP 메서드가 의미에 맞게 사용되었는가? (GET은 조회, POST는 생성 등)
- [ ] 응답 상태 코드가 적절한가? (200 + error body 안티패턴 없는가?)
- [ ] Bean Validation (`@Valid`, `@NotBlank` 등)이 DTO에 적용되어 있는가?

### 4. 예외 처리
- [ ] 빈 catch 블록이 없는가?
- [ ] `Exception`이나 `Throwable`을 직접 catch하지 않는가?
- [ ] 예외 변환 시 원본 예외가 cause로 전달되는가?

### 5. Null Safety
- [ ] `null`을 반환하는 대신 `Optional`을 사용하는가?
- [ ] 컬렉션 반환 시 `null` 대신 빈 컬렉션을 반환하는가?

### 출력 형식
- 문제 없으면: "✅ 리뷰 완료 - 이슈 없음"
- 문제 있으면: 카테고리별로 그룹핑, 각 이슈에 파일:라인 표시, 수정 제안 포함
```

이 하나의 스킬만 있어도 체감이 확 다르다. Claude가 "아, 이 프로젝트는 Spring Boot니까 이것들을 확인해야 하는구나"를 알게 된다.

#### 예제: Conventional Commit 스킬

~~~yaml
# .claude/skills/java-commit/SKILL.md
---
name: java-commit
description: 자바 프로젝트용 커밋 메시지 생성. "커밋해줘", "commit"이라고 하면 자동 실행.
disable-model-invocation: true
---

## 커밋 메시지 규칙

staged 변경사항을 분석하여 Conventional Commits 형식으로 메시지를 생성한다.

### 형식
  <type>(<scope>): <description>
  [optional body]

### Type 분류
| type | 사용 시점 |
|------|----------|
| feat | 새로운 기능 |
| fix | 버그 수정 |
| refactor | 리팩토링 (기능 변경 없음) |
| test | 테스트 추가/수정 |
| docs | 문서 변경 |
| perf | 성능 개선 |
| build | 빌드/의존성 변경 (pom.xml, build.gradle) |

### Scope 규칙
- 모듈명 사용: core, api, common, batch
- 없으면 생략 가능

### 자바 프로젝트 특화 규칙
- pom.xml이나 build.gradle 변경 → type은 build
- application.yml/properties 변경 → 관련 기능의 type 따라감
- Entity 변경 → body에 "DB 마이그레이션 필요 여부" 명시
- 테스트만 추가 → test (feat이 아님)

### 절차
1. git diff --staged 읽기
2. 변경 분석 후 커밋 메시지 초안 작성
3. 사용자에게 보여주고 승인 후에만 커밋 실행
~~~

`disable-model-invocation: true`를 설정했다. 커밋은 사용자가 직접 `/java-commit`으로 호출할 때만 실행된다. Claude가 코드 수정 중에 멋대로 커밋하는 일은 없다.

#### 예제: 마이그레이션 체크 스킬

Java 8에서 17로, Spring Boot 2에서 3으로 넘어가는 프로젝트가 아직도 많다. 이때 유용한 스킬:

```yaml
# .claude/skills/migration-check/SKILL.md
---
name: migration-check
description: 자바/스프링 버전 마이그레이션 시 호환성 체크. "마이그레이션", "버전 업그레이드"라고 하면 자동 실행.
---

## 마이그레이션 호환성 매트릭스

### Spring Boot 2 → 3 핵심 변경사항
| 변경 | Before | After |
|------|--------|-------|
| 패키지 | `javax.*` | `jakarta.*` |
| URL 매칭 | 후행 슬래시 자동 매칭 | 정확히 매칭 |
| Spring Security | `WebSecurityConfigurerAdapter` | `SecurityFilterChain` Bean |
| Properties | `spring.redis.*` | `spring.data.redis.*` |

### Java 8 → 17 활용 포인트
| 기능 | 적용 가능 코드 |
|------|---------------|
| `var` (10+) | 로컬 변수 타입 추론 |
| `record` (16+) | DTO, Value Object |
| `sealed class` (17+) | 제한된 상속 계층 |
| Text Block (15+) | SQL, JSON 문자열 |
| Pattern Matching instanceof (16+) | 캐스팅 간소화 |

### 절차
1. pom.xml/build.gradle에서 현재 Java/Spring Boot 버전 확인
2. 대상 버전과의 차이 분석
3. 변경이 필요한 파일 목록 + 구체적 수정 사항 제시
4. OpenRewrite 자동 마이그레이션 명령어 제안
```

## 실전: 내가 만든 스킬로 Spring Boot 프로젝트 리뷰해보기

말로만 하면 와닿지 않으니, 실제로 스킬이 적용된 Claude에게 코드를 던져보자.

### 시나리오: 주문 서비스 코드 리뷰

이런 코드가 있다고 치자:

```java
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Order createOrder(OrderRequest request) {
        // 주문 생성
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setItems(request.getItems());
        order.setStatus("PENDING");
        orderRepository.save(order);

        // 결제 처리
        paymentService.processPayment(order);

        // 알림 전송 (외부 API 호출)
        notificationService.sendOrderConfirmation(order);

        return order;
    }

    public Order getOrder(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        return order;
    }

    public void cancelOrder(Long id) {
        try {
            Order order = orderRepository.findById(id).get();
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            paymentService.refund(order);
        } catch (Exception e) {
            System.out.println("주문 취소 실패");
        }
    }
}
```

spring-review 스킬이 로드된 Claude라면 이렇게 잡아낸다:

**🔴 트랜잭션 문제 (2건)**

1. `createOrder()`: 알림 전송(외부 API)이 `@Transactional` 범위 안에 있다. 알림이 느리면 DB 커넥션을 오래 물고 있게 된다. 외부 호출은 트랜잭션 밖으로 분리해야 한다.

2. `cancelOrder()`: `@Transactional`이 없다. 주문 상태 변경과 환불이 원자적으로 실행되지 않는다.

**🔴 Null Safety (1건)**

3. `getOrder()`: `orElse(null)` 대신 `Optional`을 반환하거나, `orElseThrow()`로 의미 있는 예외를 던져야 한다.

**🔴 예외 처리 (2건)**

4. `cancelOrder()`: `Exception`을 catch하면서 원인 정보를 모두 버린다. `System.out.println` 대신 로거를 사용하고, 예외를 다시 던지거나 처리해야 한다.

5. `cancelOrder()`: `.get()` 직접 호출은 `NoSuchElementException` 위험이 있다.

**🟡 API 설계 (1건)**

6. `@Autowired` 필드 주입 대신 생성자 주입을 사용하면 테스트가 쉬워지고 불변성이 보장된다.

**수정 제안:**

```java
@Service
@RequiredArgsConstructor  // 생성자 주입
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setItems(request.getItems());
        order.setStatus("PENDING");
        orderRepository.save(order);
        paymentService.processPayment(order);
        return order;
        // 알림은 트랜잭션 밖에서 처리
    }

    // 트랜잭션 커밋 후 알림 전송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        notificationService.sendOrderConfirmation(event.getOrder());
    }

    @Transactional(readOnly = true)  // 읽기 전용 트랜잭션
    public Optional<Order> getOrder(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        paymentService.refund(order);
    }
}
```

스킬 없이는 "일반적인 코드 리뷰"만 해주지만, 스킬이 있으면 **자바 시니어 개발자 수준의 체크리스트**를 자동으로 적용한다. 이 차이가 핵심이다.

## 설계 철학: 왜 이렇게 만들었을까

claude-code-java의 설계 원칙을 보면 꽤 신중하다:

### 1. Human-in-the-loop

> AI가 제안하고, 개발자가 승인한다. 자동 커밋은 없다.

설정 파일에서도 이 원칙이 드러난다. `mvn test`는 자동 승인하지만, `git push`는 매번 물어본다. 코드를 읽고 분석하는 건 자유롭게, 변경하는 건 반드시 확인을 거치게.

### 2. 4단계 안전 레벨

| Level | 설명 | 예시 |
|-------|------|------|
| 0 | 읽기 전용 | 코드 리뷰, 아키텍처 분석 |
| 1 | 제안 + 리뷰 | 리팩토링 제안, 버그 수정 제안 |
| 2 | 자동 적용 | 린트 이슈 자동 수정 |
| 3 | 자동 커밋 | (비권장) |

Level 0-1만 권장한다. 실무에서도 이게 맞다고 본다. AI가 아무리 잘해도 "커밋까지 알아서 해줘"는 위험하다.

### 3. 심볼릭 링크 전략

스킬을 복사가 아닌 심볼릭 링크로 연결한다. 장단이 있다:

- **장점**: 스킬을 한 곳에서 업데이트하면 모든 프로젝트에 반영
- **단점**: 잘못된 업데이트도 모든 프로젝트에 전파

팀에서 쓸 때는 스킬 레포에 대한 PR 리뷰가 필수다. 실제로 이 프로젝트의 CI에는 Claude가 스킬 PR을 자동 리뷰하는 워크플로우가 있다. 스킬이 스킬을 리뷰하는 메타적인 구조.

## 내가 추천하는 세팅: 최소 시작 키트

18개 스킬이 전부 필요하진 않다. 내 경험상, **자바 개발자가 당장 체감이 오는 조합**은 이렇다:

### Step 1: 프로젝트 CLAUDE.md 작성

```markdown
# CLAUDE.md

## 프로젝트 정보
- Java 17 + Spring Boot 3.2 + Gradle
- 패키지 구조: feature-based (com.example.{feature})
- 테스트: JUnit 5 + AssertJ + Testcontainers

## 빌드/테스트
- `./gradlew build`: 전체 빌드
- `./gradlew test`: 테스트
- `./gradlew bootRun`: 로컬 실행

## 코딩 컨벤션
- DTO는 Java Record 사용
- 생성자 주입 (Lombok @RequiredArgsConstructor)
- Optional 반환 (null 반환 금지)
- 예외는 도메인별 커스텀 예외 사용
```

이것만으로도 Claude가 프로젝트 맥락을 파악한다.

### Step 2: 핵심 스킬 3개

1. **spring-review**: 코드 리뷰 (위에서 만든 것)
2. **java-commit**: 커밋 메시지 생성
3. **test-gen**: 테스트 코드 생성 가이드

이 3개면 일상 개발의 80%를 커버한다.

### Step 3: settings.json 설정

```json
{
  "permissions": {
    "allow": [
      "Bash(./gradlew *)",
      "Bash(mvn *)",
      "Bash(git status)",
      "Bash(git log *)",
      "Bash(git diff *)"
    ]
  }
}
```

빌드와 읽기 전용 git 명령만 자동 승인. 나머지는 물어보게.

## 정리: 미래의 나에게

### TL;DR

- **claude-code-java**는 Claude Code에 자바 전문 지식을 주입하는 스킬 세트 + 자동화 인프라
- **18개 스킬**이 코드 리뷰, Spring Boot, JPA, 동시성, 아키텍처, 마이그레이션 등을 커버
- **핵심 설계**: Human-in-the-loop (AI가 제안, 개발자가 승인)
- **심볼릭 링크**로 스킬을 공유하여 한 번 업데이트로 모든 프로젝트에 반영
- **최소 시작 키트**: CLAUDE.md + 코드 리뷰 스킬 + 커밋 스킬 + 빌드 자동 승인

### 진짜 중요한 건

도구를 설치하는 게 아니라 **Claude에게 "자바 개발자의 시각"을 가르치는 것**이다. 스킬은 그냥 마크다운 파일이다. 하지만 그 안에 "같은 클래스 내부 호출에서 `@Transactional`이 안 먹힌다"는 시니어의 경험이 담기면, Claude는 주니어에서 시니어로 레벨업한다.

### 주의할 점

- 스킬이 너무 많으면 오히려 혼란 → **실제로 쓰는 것만** 유지
- 심볼릭 링크 전략은 편하지만, 팀에서는 **스킬 변경에 대한 리뷰 프로세스** 필요
- AI가 만든 코드를 맹신하지 말 것 → 스킬은 "체크리스트 자동화"이지 "완벽한 코드 생성"이 아니다

### 더 파볼 거리

- [Hooks](https://docs.anthropic.com/en/docs/claude-code/hooks): 스킬 실행 전후에 자동화 연결 (린트 체크, 테스트 실행 등)
- [MCP 서버](https://docs.anthropic.com/en/docs/claude-code/mcp-servers): GitHub, DB 등 외부 데이터 소스 연결
- 팀 공유: 스킬 레포를 Git submodule로 관리하는 패턴
- [OpenRewrite](https://docs.openrewrite.org/): 스킬과 결합한 자동 마이그레이션 워크플로우

## References

- [decebals/claude-code-java - GitHub](https://github.com/decebals/claude-code-java)
- [Extend Claude with skills - Claude Code 공식 문서](https://docs.anthropic.com/en/docs/claude-code/skills)
- [Claude Code 시작하기 - 이전 글](../claude-code-getting-started)
- [Claude Code Skills 파헤치기 - 이전 글](../claude-code-skills-deep-dive)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/reference/)
- [Conventional Commits](https://www.conventionalcommits.org/)
