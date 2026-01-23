---
title: "자바 디자인 패턴 (3).  팩토리 패턴이란?"
date: 2020-12-23 08:20
categories: JAVA
toc: true
toc_sticky: true
---

하루에 하나씩 디자인 패턴 
(3) 팩토리 (Factory) 메쏘드 패턴을 알아보자, 


### 1. 팩토리 메서드 패턴이란?

1. __팩토리 메소드 패턴__ : 객체를 생성하기 위한 인터페이스를 정의 하는데 어떤 클래스의 인스턴스를 만들지는 서브클래스에서 결정하게 됌, 즉 팩토리 메소드 패턴을 이용하면, 클래스의 인스턴스를 만드는 일을 서브클래스에 맡길수 있다.

2. __추상 팩토리 패턴__ : 인터페이스를 이용하여 서로 연결된, 또는 의존하는 객체를 구성하는 클래스를 지정하지 않고도 생성


GOF정의 (위와 내용 동일)
```
팩토리 메서드패턴은 객체를 만들기 위한 인터페이스를 정의하지만 인스턴스화할 대상 클래스는 서브클래스에서 결정한다.
팩토리 메서드를 이용하면 하위 클래스에 사용하는 인스턴스화를 연기할 수 있다. 
```

- 모든 팩토리 패턴에서는 객체 생성을 __캡슐화__ 한다.
- __팩토리 메소드 패턴__ 과 __추상 팩토리 패턴__ 이 존재드

- __팩토리 메소드 패턴__ : 객체를 생성하기 위한 인터페이스를 정의하는데 어떤 클래스의 인스턴스를 만들지는 서브 클래스에서 결정하도록 하는 패턴
- __추상 팩토리 패턴__ : 인터페이스를 이용하여 서로 관련된, 또는 의존하는 객체를 구상 클래스를 지정하지 않고도 생성할 수 있다. 추상 팩터리 패턴에는 팩토리 메소드 패턴이 포함될 수 있다.



### 2. 팩토리 메서드의 장점

- 객체를 생성하는 코드를 분리하여 외부 코드와의 의존성을 낮춘다.
    - 코드에 변경이 필요할 시, 객체 생성 클래스만 수정함
    
- 인터페이스를 바탕으로 유연성과 확장성이 뛰어난 코드 제작이 가능하다.

- 객체의 __자료형__ 이 하위 클래스에 의해 결정된다.
    - 확장의 용의성
    - 상위 클래스에서 그 객체에 대한 정확한 타입을 몰라도 된다.
    
- SOLID 원칙중 (dependency inversion principle) 를 성립


### 3. 팩토리 메서드의 단점

- 새로 생성할 객체의 종류가 늘어날 때마다, 클래스를 만들어야 한다. 


### 4. 팩토리 메서드의 사용

new 를 사용하는 것은 구상 클래스의 인스턴스를 만드는 것, 이는 인터페이스가 아닌 특정 구현을 사용하게 되는것이다. 

```java
public class Example {
   public void ex(String type) {
     Animal animal;
     switch (type) {
       case "포유류" : animal = new Mammalia(); break;
       case "양서류" : animal = new Amphibia(); break;
       case "파충류" : animal = new Reptile(); break;
     }
   }
    
}
```

위와 같은 코드는, 뭔가 변경하거나 확장해야 할 필요가 있을 때 코드를 다시 추구 삭제 수정을 해야한다는 불편함이 있다. (SOLID 원칙 위배?..) 

단순히 불편함을 떠나 구상 클래스가 많아질 수록 새로운 구상 클래스가 추가될때마다 위와같은 일로 인해 에러를 발생할수 있기 때문에 위험하다.

그러므로 위와같은 변화에 대해 닫혀있는 코드는 최대한 지양해야 한다,

그럼 위의 문제를 어떻게 해결하는지 보자,

```java
public abstract class PizzaStore {
    public Pizza orderPizza(String type) {
        Pizza pizza = createPizza(type);
 
        pizza.prepare();
        pizza.bake();
        pizza.cut();
        pizza.box();
 
        return pizza;
    }
 
    abstract protected Pizza createPizza(String type);
}
 
public class ChicagoPizzaStore extends PizzaStore {
    @Override
    protected Pizza createPizza(String type) {
        if(type.equals("cheese")) {
            return new ChicagoStyleCheesePizza();
        }
        else if(type.equals("pepperoni")) {
            return new ChicagoStylePepperoniPizza();
        }
        else {
            return null;
        }
    }
}
 
public class Main {
    public static void main(String[] args) {
        PizzaStore nyPizzaStore = new NyPizzaStore();
        PizzaStore chicagoPizzaStore = new ChicagoPizzaStore();
 
        Pizza pizza = nyPizzaStore.orderPizza("cheese");
        System.out.println(pizza.getName() + "피자를 주문 했습니다.");
        System.out.println("===================");
        pizza = chicagoPizzaStore.orderPizza("pepperoni");
        System.out.println(pizza.getName() + "피자를 주문 했습니다.");
    }
}
```

의존성 뒤집기 원칙 (Dependency Inversion Principle) : 추상화 된 것에 의존하도록 만들기
구상 클래스에 의존 하도록 만들지 말고, 추상클래스나 인터페이스와 같이 추상적인 것에 의존하는 코드를 만들어야 한다.

#### 4. 추상 팩도리 메서드

추상 팩터리 패턴은 많은 수의 연관된 서브 클래스를 특정 그룹으로 묶어 한번에 교체할 수 있도록 만든 디자인 패턴이다. 예를 들어 특정 라이브러리를 배포하는데 OS별로 지원하는 기능이 상이 하다면 추상 팩토리 매서드 패턴을 사용해 OS별 기능 변경을 통합적으로 처리할 수 있다.

### 5. 추상 팩토리 매서드 코드

`interface MachineA
```java
public interface  MachineA {
    public void process();
}
````

MachineA1 class 
```java
public class MachineA1 implements MachineA {
    public void process() {
        System.out.println("execute Machine1");
    }
}
```

MachineA2 class
```java
public class MachineA2 implements MachineA {
    public void process() {
      System.out.println("execute Machine2");
    }
}
```


`interface MachineA
```java
public interface  MachineB {
    public void process();
}
````

MachineA1 class
```java
public class MachineB1 implements MachineB {
    public void process() {
        System.out.println("execute Machine1");
    }
}
```

MachineA2 class
```java
public class MachineB2 implements MachineB {
    public void process() {
      System.out.println("execute Machine2");
    }
}
```

MachineFactory class
```java
public interface MachineFatory {
    public MachineA getMachineA();
    public MachineB getMachineA();
}
```
MachineFactoryA class
```java
public class MachineFactoryA implements MachineFactory{
	@Override
	public MachineA getMachineA() {
		return new MachineA1();
	}
	@Override
	public MachineB getMachineB() {
		return new MachineB1();
	}
}
```

MachineFactoryA class
```java
public class MachineFactoryB implements MachineFactory{
	@Override
	public MachineA getMachineA() {
		return new MachineA2();
	}
	@Override
	public MachineB getMachineB() {
		return new MachineB2();
	}
}
```

