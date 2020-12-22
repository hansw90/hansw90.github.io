---
title: "자바 디자인 패턴 (0).  디자인 패턴이란?"
date: 2020-09-30 00:48
categories: JavaDesignPattern

---

갑자기 올리는 디자인 패턴 (2) 싱글턴 패턴 Singleton Pattern

싱글턴 패턴은 자바 static 포스팅에서 이미 다루었었기 때문에, 여기선 간단히 개념만 다루도록 한다.



### 1. 싱글턴 패턴???

싱글턴 패턴은 자바 디자인 패턴중 생성패턴에 속한다.

싱글턴은 오직 하나의 인스턴스를 보장하고, 어디에서든 이 인스턴스에 접근이 가능하도록 만들어주는 디자인 패턴이다. 원래 이름 부터 싱글턴인것이 "단 하나의"원소만을 가진 집합이라는 수학 이론에서 유래 되었다고 한다.

즉, 최초 하나의 Single 인스턴스만 생성하고, 이후에는 이 인스턴스를 getInstance()를 통해 참조하게 된다.

전역적으로 하나의 인스턴스만 사용, 참조해야하는 경우에 사용한다.

우리가 사용하는 spring container 는 기본적으로 싱글턴을 따른다.


### 2. 싱글턴의 장점vs단점
__장점__
- 하나의 인스턴스를 사용 -> 메모리 낭비를 방지
- 전역 인스턴스 -> 다른 클래스의 인스턴스들이 데이터를 공유

__단점__
- 싱글턴 인스턴스가 너무 많은 일을 하거나 많은 데이터를 공유시킬 경우, (SOLID 원칙중 SRP, OCP 원칙 위배)

### 3. 그러면 어디에 쓰는가?
- 공토된 객체를 여러 개 생성해서 사용하는 상황
- 전역에서 사용될 객체를 만들어야 하는 상황 (EX : logger)


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
    private static SingleObject instance = new SingleObject();
    
    // private 생성자를 통해 내부에서만 호출하게하고 다른곳에서 new 를 통해 새로운 인스턴스를 막아둔다.
    private SingleObject(){}

    public static SingleObject getInstance() {
        return instance;
    }
}
```
