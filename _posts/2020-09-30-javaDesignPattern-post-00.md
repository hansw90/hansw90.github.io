---
title: "자바 디자인 패턴 (0).  디자인 패턴이란?"
date: 2020-09-30 00:48
categories: JavaDesignPattern

---

갑자기 올리는 디자인 패턴 (0)


### 0. 들어가기전에
왜 갑자기 Java 기초 포스팅 중간에 디자인 패턴에 대해 올리냐?,  
회사 신입으로 들어와 5년만에 자바를 다시 만지다보니,, 구글에서 알려주는데로 + 내 의식의 흐름대로... 개발을 하고있다..   
물론, 돌아가긴 돌아가는데, 나중가서 보면 이게 왜 이렇게 만들었는지, 어떻게 재활용 해야하는지 나조차도 모르는 경우가 많았다..  
그래서 이번 디자인 패턴 포스팅은 하루에 하나씩 아래와 같은 디자인 패턴들을 알아보고, 실무에서 어떻게 적용할지(언제 써야할지)까지 알아보려 한다.


### 1. 디자인 패턴이란?
디자인 패턴이란 기존 환경 내 반족적으로 일어나는 문제들을 어떻게 효과적으로 풀어나갈 것인가에 대한 해결방법과 같다. 
[[GoF의 디자인패턴]](https://w3sdesign.com/GoF_Design_Patterns_Reference0100.pdf) 에선 디자인 패턴을 
- Creation Patterns (생성패턴)
- Structural Patterns (구조패턴)
- Behavioral Patterns (행동패턴)
로 나누고 있다.

물론 GoF(_Gof Design Pattern_)의 모든 패턴을 알아보면 좋겠지만 일단은 이전에 포스팅한 Singleton을 시작으로
1. Singleton Pattern (생성패턴)
2. Flyweight Pattern (구조패턴)
3. Abstract Factory (생성패턴)
4. Factory Method (생성패턴)
5. Builder Pattern (생성패턴)
6. Observer Pattern (행동패턴)
7. Adapter Pattern (구조패턴)
8. Facade Pattern (구조패턴)
9. Decorator Pattern (구조패턴)
10. Strategy Pattern (행동패턴)

등을 알아볼것이다. 위 순서외 더 알아보고 싶은게 있다면 추가해서 올리려 한다. 


### 2. 이번 디자인패턴 포스팅 집중 네가지 요소

- __패턴의 이름은 해당 패턴의 솔루션을 담고 있는 경우가 많다. 따라서 설계에 대한 생각을 더욱 쉽게 할 수 있고, 개발자들 간의 의사소통이 원할해진다.__
- __언제 패턴을 사용하는가를 서술하며 해결할 문제와 그 배경을 기술한다.__
- __어떻게 해결할 수 있을 것인가에 대해 해결책을 제시하고, Java를 통해 예제 코드를 다룬다__.
- __디자인 패턴을 적용해서 얻는 결과와 장단점을 서술한다.__


### 3. Need To

Java Class에 대한 기본 개념 정도만 알고 있어도 진행 가능합니다. (저도 이상태로 진행합니다. 같이 공부합시다)
아래와 같은 개발환경에서 진행됩니다.
```
JDK1.8, STS4(Eclipse)
```

시작하기전 객체나 필수 용어에 대한 글은  

[[Java 객체지향 디자인 패턴] 1. 객체지향 모델링 / UML, 클래스 다이어그램, 연관 관계, 일반화 관계, 집합 관계, 의존 관계, 실체화 관계](https://blog.naver.com/PostView.nhn?blogId=1ilsang&logNo=221104669002&parentCategoryNo=&categoryNo=59&viewDate=&isShowPopularPosts=true&from=search)
를 참고 해주세요
