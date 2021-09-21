---
title: "(1) JPA 영속성 관리"
date: 2021-09-16-00:00:00 -0000
categories: JAVA JPA
---

회사에서 사전관리 작업을 하는도중 사전에 워드를 일괄 등록하고, 일괄 수정 삭제를 해야할 일이 발생했다.

말이 일괄등록이지, 하나하나씩 값을 검증하고, 에러가 나면 모든걸 다시 롤백을 해야 되기도 하고,

JPA 쓰면서 영속성 관리를 잘 모르다 보니, Transactional 의 개념도 확 오지가 않았다. 그래서 이번엔 영속성 관리에 대해 알아보고자 한다.


### 1. 영속성 관리

JPA가 제공하는 기능

- 엔티티와 테이블을 매핑하는 __설계부분__
- 매핑한 엔티티를 실제 __사용하는 부분__

매핑한 엔티티를 엔티티 매니저를 통해 어떻게 사용하는가?

엔티티 매니저는 엔티티를 저장, 수정, 삭제하고 조회하는 등의 엔티티와 관련된 모든일을 처리한다.
=> 엔티티를 관리하는 관리자

### 2. 엔티ㅣ 매니저 팩토리 & 엔티티 매니저
```java
// Factory, 비용이 많이듬 
EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook"); 
// Factory에서 엔티티 매니저 생성, 비용이 거의 안듬 
EntityManager em = emf.createEntityManager();

//출처: https://cornswrold.tistory.com/337?category=825930 [평범한개발자노트]
```

EntityManagerFactory는 thread-safe,EntityManager는 thread-safe하지 않음
```text
thread-safe란?

스레드 세이프란 멀티 스레드 프로그래밍에서 일반적으로 어떤 함수나, 변수 혹은 객체가 여러 스레드로 부터 동시 접근이 이루어져도 프로그램의 실행에 문제가 없음을 뜻함,
보다 엄밀하게는 하나의 함수가 한 스레드로부터 호출되어 실행중 일때, 다른 스레드가 그 함수를 호출하여 동시에 함께 실행 되더라도 각 스레드에서의 함수의 수행 겨로가가 올바로 나오는 것으로 정의 함,
```
따라서 __엔티티 매니저는 여러 스레드가 동시에 접근하면 동시성 문제가 발생하므로 스레드 간에 공유를 해서는 안된다__.
- EntityManager는 보통 트랙잭션을 시작할 때 커넥션을 획득한다.
- JPA 구현체들은 EntityManagerFactory를 생성할 때 커넥션 풀도 생성한다. 

### 3. 영속성 컨텍스트
영속성 컨텍스트 = 엔티티를 영구 저장하는 환경

엔티티 매니저로 엔티티를 저장하거나 조회하면 엔티티 매니저는 영속성 컨텍스트에 엔티티를 보관하고 관리한다.
```java
em.persist(member);
```

엔티티 매니저를 사용해서 회원 엔티티를 영속성 컨텍스트에 저장한다라고 표현할 수 있다. 그 저정하는 메서드가 persist() 이다.

### 4.엔티티의 생명주기
- 비영속 (new/trasient) : 영속성 컨텍스트와 전혀 관련없는 상태 
- 영속 (managed) : 영속성 컨텍스트에 저장된 상태
- 준영속 (detached) : 영속성 컨텍스트에 저장되었다 분리된 상태
- 삭제 (removed) : 삭제된 상태


위 생명주기를 코드로 알아보자

##### 비영속
엔티티 객체를 생성했으나, 저장하지 않은 상태 (순수 객체 상태)
아직 영속성 컨텍스트에 존재하지는 않는다. 
```java
// 객체를 생성한 상태 (비영속)
Member member = new Member();
member.setMemberName("한승우");
```

##### 영속
엔티티 매니저를 통해서 엔티티를 영속성 컨텍스트에 저장한 상태 (관리되는 상태)
= 컨텍스트가 관리하는 엔티티를 영속 상태라고 한다.
```java
em.persist(meeber);
```


##### 준영속
영속성 컨텍스트가 관리하던 영속 상태의 엔티티를 영속성 컨텍스트가 관리하지 않으면 준영속 상태가 된다.
- em.detach() : 엔티티를 준영속 상태로 만든다.
- em.cloase() : 영속성 컨텍스트 닫음
- em.clear() : 영속성 컨텍스트 초기화

위 세가지 메소드를 호출하면 영속 상태의 엔티티는 준영속 상태가 된다.

###  5.영속성 컨텍스트 특
1. 영속성 컨텍스트와 데이터 베이스 저장 
    - JPA는 보통 트랙잭션을 커밋하는 순간 영속성 컨텍스트에 새로 저장된 엔티티를 데이터 베이스에 반영한다. 이를 flush라고 한다.
2. 영속성 컨텍스트가 엔티티를 관리할때 장점
    - 1차 캐시
    - 동일성 보장
    - 트랜잭션을 지원하는 __쓰기지연(transactional write behind)__
    - 변경 감지 (dirty checking)
    - 지연 로딩 (lazy loading)

##### 5-1. 1차 캐시 
1차 캐시가 되는 것은 오직 식별자로 쿼리할 때만이 가능하다.
__식별자란, 바로 @Id 어토테이션을 붙여둔 Key다__ 

다음 예제를 살펴보자 
```text
Person(id = 1, name = '한승우')
```
같은 Entity가 db에 있다고 가정하면
여기서 아래 쿼리를 하면 어떤 결과가 발생할까?

```java
Person person1 = repository.findByName("한승우");
Person person2 = repository.findByName("한승우");
```

person1을 쿼리할 때 ID 1번을 영속성 컨텍스트에 담았으므로 person2를 쿼리할 때는 영속성 컨텍스트에 있는 엔티티를 가지고 올까?
결과는 person1, person2 모두 sql에 쿼리를 날려 해당 앤티트를 가지고 온다.

쿼리를 할때, 식별자가 아니면 엔티티 매니저는 쿼리를 해보기 전까지 같은 식별자를 가지고 있는지 알수 없다

식별자로 쿼리하면 1차 캐시가 적용되는것은 맞지만 아래와 같이 사용하는건 주의가 필요하다.
```java
Person person1 = repository.findById(1);
Person person2 = repository.findById(1);
```
Id 필다가 식별자라도 위와 같은 __Spring Data Jpa의 쿼리 메소드를 사용해서 쿼리하면 위와 마찬가지로 1차 캐시가 적용되지 않는다.__ (스프링 부트 2.0버전 이하 해당)

스프링 부트 2.0 부터는 findById가 기본 제공되므로 그대로 사용해도 좋다, 그 이하 버전에서는 findById 대신 아래와 같이 findOne을 사용해야 한다.
```java
Person person1 = repository.findOne(1);
Person person2 = repository.findOne(1);
```

##### 5-2. 쓰기지연
영속성 컨텍스트는 쓰기 지연을 지원한다.

이는, 한 트랙잭션 안에서 이뤄지는 UPDATE나 SAVE의 쿼리를 쓰기 지연 저장소에 가지고 있다가 트랜잭션이 커밋되는 순간 한번에 DB에 날리는 것을 말한다.
이로써 얻을 수 있는 장점은 DB 커넥션 시간을 줄일수 있고,  한 트랜잭션이 테이블에 접근하는 시간을 줄일 수 있다는 장점이 있다. 


__1. 단순 SAVE__
```java
@Transactional
public void test() { 
	repository.save(productItem);  
	println("로그") ;
}
```
개념 대로라면 로그보다 작성한 insert쿼리가 늦게 나가야 된다, 하지만 console을 확인해 보면 예상과는 다르게 insert쿼리가 즉시 나가고 그 이후 로그가 출력이 된다.

__원인__
엔티티가 영속성 상태가 되려면 식별자가 꼭 필요하다.
그런데 식별자가 생성전략을 IDENTITY로 사용하면 데이터베이스에 실제로 저장을 해야 식별자를 구할 수 있으므로 Insert 쿼리가 즉시 데이터 베이스에 전달 된다. 
따라서 위의 경우엔 쓰기 지연을 사용하는것이 불가능 하다.

__2. 단순 UPDATE 만을 하는경우__
```java
@Transactional
public void test() { 
    
    Member member = memberRespository.findById(1);
    member.userAge = 10;
    
	repository.save(memeber);  
	println("로그") ;
}
```

그렇다면 위의 쿼리는 언제 날라갈라?
이것은 우리가 알던대로 쓰기지연이 작동되어 트랜잭션이 종료되고 커밋되는 순간 데이터베이스에 전달이 된다.
즉 내가 삽입한 로그가 먼저 출력되고,  그 이후 UPDATE 쿼리가 작동한다.


__ 3. 단순 UPDATE이후 식별자가 아닌 필드 쿼리
```java
@Transactional
public void test() { 
    
    Member member = memberRespository.findById(1);
    member.userAge = 10;
    
	repository.save(memeber);  
	println("로그") ;
	
	Member meber2 = memberRepository.findByName("한승우")
}
```

이러면 어트 타이밍에 UPDATE 쿼리가 날라 갈까?
식별자가 아닌 필드로 조회를 하면 조회 쿼리를 하기전에 쓰기 지연 저장소에 있던 UPDATE쿼리를 날리고 그 이후에 조회를 한다.
이것은 트랜잭션 Isolation Level을 낮춰도 동일하게 작동된다.

### 6. 엔티티 조회
영속성 컨텍스트는 내부에 캐시를 가지고 있다. 이 캐시를 1차 캐시라 하며 영속 상태의 엔티티는 모두 이곳에 저장된다.
__1차 캐시__
영속성 컨텍스트 내부에 Map이 존재하며 키는 __@Id__ (식별자), 갑승 엔티티 인스턴스이다.

```java
// 엔티티 생성(비영속) 
Memeber member = new Member(); 
member.setId(1); 
member.setUsername("한승우"); 
// 엔티티 영속 
em.persis(member);
```

##### 6-1. 1차 캐시에서 조회
```java
Member member = em.find(Member,class, "member1");
```

em.find() 호출시, 엔티티가 1차 캐시에 존재하지 않는다면 엔티티 매니저는 데이터베이스를 조회해서 엔티티를 생성한다/ 그리고 1차 캐시에 저장된 후 영속 상태의 엔티티를 반환하게 된다.

##### 6-2 데이터베이스에서 조회
em.find() 호출시, 엔티티가 1차 캐시에 존자하면 엔티티 매니저는 데이터베이스를 조회해서 엔티티를 생성한다. 그리고 1차 캐시에 저장한후 영속 상태의 엔티티를 반환하게 된다.

