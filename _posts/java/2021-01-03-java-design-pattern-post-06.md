---
title: "자바 디자인 패턴 (6). 옵저버 패턴 (Observer Pattern)"
date: 2021-01-03 11:09
categories: JAVA
toc: true
toc_sticky: true
---

2021년 2번째 포스팅, 객체 상태 변화를 관찰하는 자바 옵저버 패턴에 대해 알아보자, (변태 스토커)

### 1. 옵저버 패턴이란? 

![옵저버 패턴](https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/Observer.svg/854px-Observer.svg.png)

우리는 객체지향 프로그래밍 할때, 객체들 사이의 다양한 처리를 하는 경우가 있다. 그 중 한예로는 특정 객체의 상태가 바뀔 경우 다른 객체들에게 이 변경 상태를 알려줘야할 경우는 매우 빈번하다. 상태를 가지고 있는 주체 객체와 상태의 변경을 알아야 하는 __관찰 객체(Observer object)__ 가 존재하며 이들의 관계는 1:N 이 된다. 
이런 서로의 정보 교환의 관계에서 정보 단위가 크거나 객체 규모가 클수록 즉, 관계들이 복잡할 수록 점점 구현하기가 어려워지고 복잡성이 매우 증가하게 된다.
이러한 기능을 쉽게 구현할 수 있도록 제시한것이 바로 __옵저버 패턴__ 이다.

다른 이름으론 종속자(Dependent), 게시-구독(Publish-subsribe) 등

### 2. 옵저버 패턴의 사례
1. 외부에서 발생한 이벤트 (ex 사용자 입력) 에 대한 응답. (이벤트 기반 프로그래밍)
2. 객체의 속성 값 변화에 따른 응답, 종종 콜백은 속성 값 변화를 처리하기 위해 호출될 뿐 아니라 속성 값 또한 바꾼다.
   (잘못쓰면 이벤트 연쇄의 원인이 된다.)
3. 실생활에서 옵저버 패턴은 뭐가 있을까? 
   (신문 구독?)

### 3. 옵저버 패턴의 장점
옵저버 패턴을 사용하면 주체 및 옵저버 모두를 독립적으로 변형하기 쉽다. 감시자를 재사용하지도 않고 주체를 재사용할 수 있고, 주체 없이도 감시자를 재사용할 수 있다. 
또한 주체나 감시자의 수정 없이도 감시자를 추가할 수 있다.

아래는 옵저버 패턴의 장점이다.

1. Subject와 Observer __클래스 간에는 추상적 결합도만 존재__ 하게 된다.
2. __브로드캐스트 방식__ 의 교류가 가능
   (일반적 요청과는 달리, 옵저버 패턴에서 주체가 보내는 통보는 구체적인 수신자를 지정할 필요가 없다.)
3. 예측하지 못한 정보를 갱신

### 4. 자바 내장 옵저버 패턴 

자바 내장 Observable, Observer는 Java SE 9 버전 부턴 Deprecated 되었음을 알린다.
그 이유에 대해선 4-4 에서 설명하도록 하겠다.

- java.util.Observer
- java.util.Observable

자바 내장 옵저버 패턴은 푸시 방식, 풀방식 모두 사용가능하다.

##### 4-2. 자바 내장 옵저버 패턴 사용 방법

1. 옵저버 목록 추가 삭제 
   1. java.utill.Observer 인터페이스를 구현
   2. java.utill.Observavle 객체의 addObserver()메서드를 호출하면 옵저버 목록에 추가
   3. java.utill.Observavle 객체의 deleteObserver()메서드를 호출하면 옵저버 목록에 삭제

2. 연락 방법
   1. java.utill.Observable 상속을 받는 주제 클래스에 setChanged() 메소드를 호출해 객체의 상태가 바뀐 것을 알린다.
   2. notifyObservers() 또는 notifyObserver(Object arg) 메소드를 호출하여 사용한다.
   

3. 연락을 받는 방법
   1. update(Observer o, Object arg) 메소드를 구현
   - Observer o : 연락을 보내는 주제 객체 인자 
   - Object arg : notifyObservers(Object arg) 메서드에서 인자로 전달된 데이터 객체


##### 4-3 자바 내장 옵저버 패턴 예제 코드
SubscribeController.class
```java

// PlayerController 가 Subject
public class SubscribeController extends Observable {
    // 감시의 대상
    private boolean subscribeStatus;

    public SubscribeController() {
    }

    // 데이터를 전달 받아 플래그 값을 변경후
    // 새로운 데이터가 들어왔을을 알린다. 자기가 직접 캐치하는게 아닌데 옵저버??? 좀 이상한구먼
    public void setSubscribe(boolean subscribeStatus) {
        this.subscribeStatus = subscribeStatus;
        setChanged();
        notifyObservers();
    }

    // 실행 여부 플래그 값 반환
    public boolean getSubscribe() {
        return subscribeStatus;
    }
}
```
Subscriber1.class
```java
public class Subscriber1 implements Observer {
    Observable observable; // 등록될 Observable
    private boolean subscribeStatus; // 실행 여부

    public Subscriber1(Observable o) {
        this.observable = o;
        observable.addObserver(this); // 옵저버에 현재 클래스를 등록한다.
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof SubscribeController) {
            SubscribeController controller = (SubscribeController) o;
            this.subscribeStatus = controller.getSubscribe();
            act(); // 변화후 내가 실행하고 싶은 작업
        }
    }

    public void act() {
        if (subscribeStatus) {
            System.out.println("Subscriber1 구독 시작");
        } else {
            System.out.println("Subscriber1 구독 취소");
        }
    }

}
```

Subscriber2.class
```java
public class Subscriber2 implements Observer {

    Observable observable; // 등록될 Observable
    private boolean subscribeStatus; // 실행 여부

    public Subscriber2(Observable o) {
        this.observable = o;
        observable.addObserver(this); // 옵저버에 현재 클래스를 등록한다.
    }

   @Override
    public void update(Observable o, Object arg) {
        if (o instanceof SubscribeController) {
            SubscribeController controller = (SubscribeController) o;
            this.subscribeStatus = controller.getSubscribe();
            act(); // 변화후 내가 실행하고 싶은 작업
        }
    }

    public void act() {
        if (subscribeStatus) {
            System.out.println("Subscriber2 구독 시작");
        } else {
            System.out.println("Subscriber2 구독 취소");
        }
    }
    // 별도의 구독 취소 methdd를 만듬
    public void cancleSubscribe() {
        observable.deleteObserver(this);
        System.out.println("Subscriber2 구독 완전 취소");
    }
}
```
Test.class
```java
public class Test {

    public static void main(String[] args) {
        SubscribeController controller = new SubscribeController();

        Subscriber1 subscriber1 = new Subscriber1(controller);
        Subscriber2 subscriber2 = new Subscriber2(controller);

        System.out.println("---------모든 구독 일시 정지 ------");
        controller.setSubscribe(false);

        System.out.println("---------모든 구독 다시 시작 ------");
        controller.setSubscribe(true);

        System.out.println("________Subsciber2 구독 취소______");
        subscriber2.cancleSubscribe();

        System.out.println("---------모든 구독 일시 정지 ------");
        controller.setSubscribe(false);

        System.out.println("---------모든 구독 다시 시작 ------");
        controller.setSubscribe(true);
    }
}
```

결과
```
---------모든 구독 일시 정지 ------
Subscriber2 구독 취소
Subscriber1 구독 취소
---------모든 구독 다시 시작 ------
Subscriber2 구독 시작
Subscriber1 구독 시작
________Subsciber2 구독 취소______
Subscriber2 구독 완전 취소
---------모든 구독 일시 정지 ------
Subscriber1 구독 취소
---------모든 구독 다시 시작 ------
Subscriber1 구독 시작
```

굉장히 편리하고 유용한 기능 인것 같다.
그런데 왜 JAVA SE 9 부터는 이 기능이 deprecated 되었을까?..

Java SE 9 문서의 Observable에는 이와 같이 설명한다.
- Observer와 Observable이 제공하는 이벤트 모델이 제한적이다.
- Observable의 notify는 순서를 보장할 수 없으며, 상태 변경은 1:1로 일치하지 않는다.
- 더 풍부한 이벤트 모델은 java.beans 패키지가 제공하고 있다.
- 멀티 스레드에서의 신뢰할 수 있고 순서가 보장된 메시징은 __java.util.concurrent__ 패키지의 concurrent 자료 구조들 중 하나를 골라 쓰는 편이 낫다.
- reactive stream 스타일 프로그래밍은 __Flow API__ 를 쓰기를 권한다.


객체 지향적인 단점 
- Observable은 class이다.
   - Observable이 클래스이기 때문에 서브 클래스를 만들어야 한다는 점이 문제, 이미 다른 수퍼 클래스를 확장하고 있는 클래스에 Observable의 기능을 추가할 수가 없다. 이러한 이유로 재사용성에 제약이 생긴다.
 

### 5. 헤드퍼스트 디자인 패턴 : 옵저버 패턴 예제 

Observer.class
```java
public interface Observer {
    public void update(float temp, float humidity, float pressure);
}
```

Subject.class
```java
public interface Subject {
    public void registerObserver(Observer o); // Subject에 Observer를 구독자로 등록
    public void removeObserver(Observer o); // Subject에 등록한 Observer의 구독을 해지
    public void notifyObservers(); // Subject에서 모든 Observer에 정보를 전달한다.
}
```

DisplayElement.class
```java
public interface DisplayElement {
    public void display();
}
```

WeatherData.class
```java
public class WeatherData implements  Subject{

    private ArrayList<Observer> observers;
    private float temperature;
    private float humidity;
    private float pressure;

    public WeatherData() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        int i = observers.indexOf(o);
        if (i >= 0) {
            observers.remove(i);
        }
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(temperature, humidity, pressure);
        }
    }

    public void measurementsChanged() {
        notifyObservers();
    }

    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        measurementsChanged();  // 변경이 발생할 때, 알림을 돌리는 방법 선택
    }
}
```

CurrentConditionDisplay
```java
public class CurrentConditionsDisplay implements Observer, DisplayElement{
    private int id;
    private float temperature;
    private float humidity;
    private Subject weatherData;

    public CurrentConditionsDisplay(Subject weatherData, int id) {
        this.id = id;
        this.weatherData = weatherData;
        weatherData.registerObserver(this);
    }

    @Override
    public void update(float temp, float humidity, float pressure) {
        this.temperature = temp;
        this.humidity = humidity;
        display();  // 편의상 여기에 배치
    }

    @Override
    public void display() {
        System.out.println("장비 ID: " + id + ", 현재 기온: " + temperature + "도, 습도: " + humidity + "%");
    }
}
```