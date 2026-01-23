---
title: "자바 디자인 패턴 (8). 전략 패턴"
date: 2021-01-10 22:00
categories: JAVA
toc: true
toc_sticky: true
---

내일 일요일 실화냐,, 프로젝트에서 굉장히 많이 사용되는, 전략적으로 행위를 유연히 변경 가능한 전략 패턴에 대해 알아보자,

![이미지](https://upload.wikimedia.org/wikipedia/commons/4/45/W3sDesign_Strategy_Design_Pattern_UML.jpg)

### 0. 목차
 
### 1. 전략 패턴 (Strategy Pattern) 이란?

__전략 패턴__ Strategy Pattern 이란. 객체들이 할 수 있는 행위 각각에 대해 전략 클래스를 생성하고, 유사한 행위들을 캡슉ㄹ화 하는 인터페이스를 정의하여, 
객체의  행위를 동적으로 바꾸고 싶은 경우 직접 행위를 수정하지 않고 전략적으로 바꿔 주기만 함으로써 행위를 유연하게 확장하는 방법을 말한다.

간단히 말하면 객체가 할 수 있는 행위들을 각각을 전략으로 만들어 두고, 동적으로 행위의 수정이 필요한 경우 전략을 바꾸는 것만으로 행위의 수정이 가능하도록 만드는 패턴이라 할 수 있다.

각각의 알고리즘군을 교환이 가능하도록 별도로 정의하고 각각 캡슐화 한 후 서로 교환해서 사용할 수 있는 패턴이다.

이에 대한 장점은 아래와 같다.

### 2. 전략 패턴을 사용하는 이유

- 코드의 중복을 방지할 수 있다.
- 런타임(Runtime)시 타겟 메서드를 변경할 수 있다.
- 확장성(신규 클래스) 및 알고리즘의 변경이 용이하다.

즉 '프로젝트 전체에서 변경이 일어나지 않는 부분에서 변경이 일어나는 부분을 찾아서 따로 캡슐화 한다.' 라고 생각하자


### 3. 예제
```java
public interface CarMoveBehavior {
    public void action();
}
```

```java
class UpBehavior implements CarMoveBehavior {
    @Override
    public void action() {
        System.out.println("Up!");
    }
}

class DownBehavior implements CarMoveBehavior {
    @Override
    public void action() {
        System.out.println("Dwon!");
    }
}

class LeftBehavior implements CarMoveBehavior {
    @Override
    public void action() {
        System.out.println("Left!");
    }
}

class RightBehavior implements CarMoveBehavior {
    @Override
    public void action() {
        System.out.println("Right!");
    }
}
```

```java
public class Car {
    private CarMoveBehavior carMoveBehavior;

    public Car(CarMoveBehavior carMoveBehavior) {
        this.carMoveBehavior = carMoveBehavior;
    }
    
    public void move() {
        carMoveBehavior.action();
    }
    
    public void setCarMoveBehavior(CarMoveBehavior carMoveBehavior) {
        this.carMoveBehavior = carMoveBehavior;
    }

}
```

```java
public class Main {
    public static void main(String[] args) {
        Car car1 = new Car(new UpBehavior);
        car1.move();
        
        Car car2 = new Car(new DownBehavior);
        car2.move();

        Car car3 = new Car(new LeftBehavior);
        car3.move();
    
        Car car4 = new Car(new RightBehavior);
        car4.move();
    }
}
```