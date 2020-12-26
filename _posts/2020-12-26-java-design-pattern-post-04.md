---
title: "자바 디자인 패턴 (1).  빌더 패턴"
date: 2020-12-26 20:12
categories: JavaDesignPattern
---

일어나니 20시

객체의 생성 방법과 표현 방법을 분리하기 위한 빌더 패턴에 대햐 알아보자,


### 1, 빌더 패턴 (Builder Pattern) 이란?

```
생성자에 매개 변수가 많다면 빌더를 고려하자
- Effective JAVA
```
빌더 패턴이란 복작 객체의 생성 과정과 표현 방법을 분리하여 동일한 생성 절차에서 서로 다른 표현 결과를 만들 수 있게 하는 패턴이다.

![빌더패턴](https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/Builder_UML_class_diagram.svg/700px-Builder_UML_class_diagram.svg.png)
-출처 : 위키피디아

 빌더 패턴은 추상 팩토리 패토리 패턴이나 팩ㄷ토리 메서드 패턴과는 차이가 있다. 빌더 패턴도 새로운 객체를 만들어서 반환하는 패턴이긴 하지만 실제 동작 방식은 조금 차이가 있다.

 빌더 패턴은 생성자에 들어갈 매개 변수가 많든 적든 차례 차례 매개 변수를 받아들이고 모든 매개 변수를 받은 뒤 이 변수드을 통합해 한번에 사용한다,
 
 빌더 패턴은 이전에 배운 정적 팩토리 메서드 패턴의 단점과 자바 빈즈 패턴의 단점을 보완 하기 위해 나왔다, 
 
### 2. 장점
- 각 인자가 어떤 의미인지 알기가 쉽다.
- setter 메소드가 없으므로 변경 불가능 객체를 만들 수 있다.
- 한 번에 객체를 생성하므로 객체 일관성이 안깨진다.
- build() 함수가 잘못된 값이 입력되었는지 검증하게 할 수도 있다.

### 3. 빌더 패턴 예제 코드

NutritionFacts.class
```java

// Effective Java Builder Pattern
public class NutritionFacts {

    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;  //carbohydrate : 단백질.. 영어 공부좀 하자,


    public static class Builder {

        // 필수 인자
        private final int servingSize;
        private final int servings;

        // 선택인자, 선택적 인자는 기본값을 설정합니다.
        private int calories;
        private int fat;
        private int sodium;
        private int carbohydrate;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int val) {
            calories = val;
            return this;   // 이방법을 사용하면 . 으로 체인 사용이 가능하다.
        }

        public Builder fat(int val) {
            fat = val;
            return this;
        }

        public Builder sodium (int val) {
            sodium = val;
            return this;
        }

        public Builder carbohydrate(int val) {
            carbohydrate = val;
            return this;
        }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder) {
        servingSize  = builder.servingSize;
        servings     = builder.servings;
        calories     = builder.calories;
        fat          = builder.fat;
        sodium       = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }

    @Override
    public String toString() {
        return "NutritionFacts{" +
                "servingSize=" + servingSize +
                ", servings=" + servings +
                ", calories=" + calories +
                ", fat=" + fat +
                ", sodium=" + sodium +
                ", carbohydrate=" + carbohydrate +
                '}';
    }
}

```

Main.class
```java
public class Main {
    public static void main(String[] args) {

        NutritionFacts.Builder builder = new NutritionFacts.Builder(100,200);
        builder.calories(300);
        builder.sodium(400);
        builder.carbohydrate(500);
        NutritionFacts bigMac = builder.build();
        
        System.out.println(bigMac);

        ///////////////// 아래 방법으롣도 사용이 가능하다. ////////////////////////

        NutritionFacts cocaCola = new NutritionFacts
                .Builder(99, 99)
                .calories(99)
                .sodium(99)
                .carbohydrate(99)
                .build();

        System.out.println(cocaCola);

    }
}
```

### 4. 마무리
위에 코드를 보면 알 수 있듯 빌더 패턴을 사용하면 직관적인 생성 패턴을 만들수가 있다.

또한 Lombok에서는 @Builder 라는 Annotation 을 제공 하고 있다. 이걸 이용하면 조금더 편하게 Builder 패턴을 사용할 수 있

```java
@Builder
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;
}
```다

