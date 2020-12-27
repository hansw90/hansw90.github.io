---
title: "자바 디자인 패턴 (2).  싱글턴 패턴이란?"
date: 2020-12-23 00:48
categories: JavaDesignPattern

---

갑자기 올리는 디자인 패턴 (2) 싱글턴 패턴 Singleton Pattern

싱글턴 패턴은 자바 static 포스팅에서 이미 다루었었기 때문에, 여기선 간단히 개념만 다루도록 한다.



### 1. 싱글턴 패턴???

싱글턴 패턴은 자바 디자인 패턴중 생성패턴에 속한다.

싱글턴은 오직 하나의 인스턴스를 보장하고, 어디에서든 이 인스턴스에 접근이 가능하도록 만들어주는 디자인 패턴이다. 원래 이름 부터 싱글턴인것이 "단 하나의"원소만을 가진 집합이라는 수학 이론에서 유래 되었다고 한다.

즉, 최초 하나의 Single 인스턴스만 생성하고, 이후에는 이 인스턴스를 getInstance()를 통해 참조하게 된다. 보통 함수 같은 __Stateless__ 객체 또는 본질적으로 유일한 시스템 컴포넌트를 그렇게 만든다.


__싱글톤을 사용하는 클라이언트 코드를 테스트 하는것은 어렵다.__ 싱글톤이 인터페이스를 구현하게 아니라면 mock으로 교체하는게 어렵기 때문이다.

우리가 사용하는 spring container 는 기본적으로 싱글턴을 따른다.

#### 1-2 Stateless Object란? 

[※ Stateless Object란?][https://stackoverflow.com/questions/9735601/what-is-stateless-object-in-java]
stackoverflow 인용

Stateless object is an instance of a class without instance fields (instance variables). The class may have fields, but they are compile-time constants (static final).

A very much related term is immutable. Immutable objects may have state, but it does not change when a method is invoked (method invocations do not assign new values to fields). These objects are also thread-safe.

Stateless object란 인스턴스 필드 (인스턴스 변수)가 없는 객체를 말한다. 클래스에 필드가 존재하는건 괜찮지만 static final 로 컴파일 단계에서 선언되는 상수들이다.

불변 객체는 상탤ㄹ 가질 수 있지만 메서드가 호출 될때 변경이 되지는 않는다. (메서드 호출은 필드의 값에 새값을 할당하지 않음) 이러한 객체는 스레드로 부터 안전하다.

#### 1-3 Stateless Object 코드
```java
class Stateless {
    void test() {
        System.out.println("Test!");
    }
}

class Stateless {
    //No static modifier because we're talking about the object itself
    final String TEST = "Test!";

    void test() {
        System.out.println(TEST);
    }
}
```
아래 객체는 state를 갖는다, 그러므로 이것은 stateless 객체는 아니다. 하지만 이 객체는 단 한번만 명시되고 또한 나중에 변하지 않는다, 이러한 객체를 우리는 immutable 객체라 한다.
```java
class Immutable {
    final String testString;
    
    Immutable(String testString) {
        this.testString = testString;
    }
    
    void test() {
        System.out.println(testString);
    }
}
```

### 2. 싱글턴의 장점vs단점
__장점__
- 하나의 인스턴스를 사용 -> 메모리 낭비를 방지
- 전역 인스턴스 -> 다른 클래스의 인스턴스들이 데이터를 공유

__단점__
- 싱글턴 인스턴스가 너무 많은 일을 하거나 많은 데이터를 공유시킬 경우, (SOLID 원칙중 SRP, OCP 원칙 위배)

### 3. 그러면 어디에 쓰는가?

- 공토된 객체를 여러 개 생성해서 사용하는 상황
- 전역에서 사용될 객체를 만들어야 하는 상황 (EX : logger)

### 4. 싱글톤 생성 방법
생성자를 private 으로 만들고 public static 맴버를 사용하여 유일한 인스턴스를 제공하게 한다.


### 5. 싱글톤 코드
SingletonPatternTest.java
```java
public class SinglePatternTest {
    public static void main(String [] args) {
        SinglePatternTest object = SingleObject.getInstance();
    }
}
```

```java
public class SingleObject {
    private static final SingleObject instance = new SingleObject();
    
    // private 생성자를 통해 내부에서만 호출하게하고 다른곳에서 new 를 통해 새로운 인스턴스를 막아둔다.
    private SingleObject(){}

    public static SingleObject getInstance() {
        return instance;
    }
}
```
