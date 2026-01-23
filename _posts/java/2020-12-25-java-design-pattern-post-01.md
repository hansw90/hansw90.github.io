---
title: "자바 디자인 패턴 (1).  정적 팩토리 메소드"
date: 2020-12-25 22:59
categories: JAVA
toc: true
toc_sticky: true
---

올해도 솔크,, 

우울한 크리스마스에 올리는 디자인 패턴 (1)  정적 팩토리 메소드

한번쯤은 들어 봤을듯한 정적 팩토리 메소드에 대해 알아보려 한다.

왜쓰는지 부터 어떻게 쓰는지 자세히 알아보자.

# 1. 정적 팩토리 메소드란?

정적 STATIC , 팩토리 FACTORY, 메서드 METHOD 란?

정적 팩토리 메서드는 객체를 캡슐화 하는 기법중 하나이다.
```
※ 객체를 캡슐화 한다는건 무슨말인가?? 
    객체 지향 프로그램에서의 캡슐화는 아래의 측면을 가지고 있다.
    - 객체의 속성(data filed)와 행위(메서드)를 하나로 묶고,
    - 실제 구현 내용 일부를 외부에 감추어 은닉한다. (외부 객체는 내부의 구조를 얻지 못함, 객체가 노출해 제공하는 필드와 메소드만 이용가능)
    - 외부의 잘못된 사용으로 객체가 손상되는 것을 막기위해 캡슐화 한다
    - 자바에선 캡슐화 멤버의 노출 여부를 접근제한자를 이용한다
    
    즉 중요한 데이터를 보존, 보호하는 것을 뜻한다.
```

좀더 구체적으로 객체를 생성하는 메소드를 static 으로 선언하는 기법이다.

자바의 valueOf가 메서드가 정적 팩토리 메서드의 한 예이다.

```
String exBoolean = String.valueOf(42);
                                                                                           
public static String valueOf(int i) {
    return Integer.toString(i);
}
public static String valueOf(long l) {
    return Long.toString(l);
}
// 등등등
```

아 그리고 우리가 아는 디자인 패턴인 팩토리 메서드 패턴과 정적 팩토리 메서드 패턴은 다른 패턴이다.

### 2. 정적 팩토리 메서드 장점
1. 이름이 있어 생성자에 비해 가독성이 좋다.
2. 호출할 때마다 새로운 객체를 생성할 필요가 없다.
3. 하위 자료형 객체를 반활할 수 있다.
4. 형인자 자료형 (parameterized type) 객체를 만들 때 편한다.

### 3. 정적 팩토리 메서드 단점
1. 정적 팩토리 메서드만 있는 클래스라면, 생성자가 없으므로 하위 클래스를 만들지 못한다.
2. 정적 팩토리 메서드는 다른 정적 메서드와 잘 구분이 되지 않는다. (문서만으로 확인하기 어려움)

### 4. 특징

##### 1. 가독성이 좋다.
```java

class FootballPlayer {
    String Name;
    Integer age;
    Integer speed;
    Integer pass;
    Integer shoot;
    Integer defense;

    public FootballPlayer( String name, Integer age, Integer speed, Integer pass, Integer shoot, Integer defense) {
        this.name = name;
        this.age = age;
        this.shoot = shoot;
        this.defense = defense;
        
    }
    
    public static FootballPlayer striker(String name, Integer age) {
        Random random = new Random();
        Integer shoot = random.nextInt(100);
        Integer defense = random.nextInt(50);        
        return new FootballPlayer(name, age, shoot, defense);                
    }

    public static FootballPlayer centerback(String name, Integer age) {
        Random random = new Random();
        Integer shoot = random.nextInt(50);
        Integer defense = random.nextInt(100);
        return new FootballPlayer(name, age, shoot, defense);
    }
}
```
생성자를 사용해 스트라이커나 센터백을 생성해야 한다면 아래와 같다.
```java
public class Ground {
    FootballPlayer messi = new FootballPlayer("messi", 33, 99, 40);
    FootballPlayer ramos = new FootballPlayer("ramos", 33, 30, 95);
}
```
이 정보만으로는 messi가 스트라이커인지 수비수인지 알수가 없다.

하지만 정적 팩토리 메서드를 사용한다면 좀 더 읽기 쉬운 코드가 된다.

```java
public class Ground {
    FootballPlayer messi = FootballPlayer.striker("messi", 33);
    FootballPlayer messi = FootballPlayer.ramos("ramos", 33);
}
```

good :) 

##### 2. 호출할 때마다 새로운 객체를 생성할 필요가 없다.

##### 3. 하위 자료형 객체를 반환할 수 있다.

이말인즉 리턴하는 객체의 타입을 유연하게 지정할 수 있다는 말이다.

```java
class OrderUtil {

    public static Discount createDiscountItem(String discountCode) throws Exception {
        if(!isValidCode(discountCode)) {
            throw new Exception("잘못된 할인 코드");
        }
        // 쿠폰 코드인가? 포인트 코드인가?
        if(isUsableCoupon(discountCode)) {
            return new Coupon(1000);
        } else if(isUsablePoint(discountCode)) {
            return new Point(500);
        }
        throw new Exception("이미 사용한 코드");
    }
}

class Coupon extends Discount { }
class Point extends Discount { }
```

할인 코드의 규칙에 따라 Coupon 과 Point 객체를 선택적으로 리턴한다.

이 방벙을 사용하려면 두 하위클래스 coupon과 print 클래스가 같은 인터페이스를 구현하거나 같은 부모 클래스를 가져야 한다.

