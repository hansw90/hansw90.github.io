---
title: "자바 디자인 패턴 (7). 어댑터 패턴 (Adapter 패턴)"
date: 2021-01-03 20:16
categories: JavaDesignPattern
---

주말이라 2개 포스팅 ~ :) 이름과 같이 끼워 연결해주는 어댑터 패턴 (adapater pattern)에 대해 알아보자 

![adapter pattern](https://upload.wikimedia.org/wikipedia/commons/e/e5/W3sDesign_Adapter_Design_Pattern_UML.jpg)

### 0. 목차

1. 어댑터 패턴이란?
2. 어댑터 패턴의 장점
3. 어댑터 패턴은 어디서 사용되나
4. 예제 Code
    1. 인스턴스에 의한 Adapter 패턴
    2. 클래스에 의한 Adapter 패턴
    
### 1. 어댑터 패턴이란?
```
한 클래스의 인터페이스를 클라이언트에서 사용하고자하는 다른 인터페이스로 변환한다.
어댑터를 이용하면 인터페이스 호환성 문제 때문에 같이 쓸수 없는 클래스들을 연결해 사용가능하다.
```
이미 제공되어 있는 것'과  '필요한 것' 사이의 '차이'를 없애주는 디자인 패턴이 Adapter 패턴이다.

- Adapter 패턴은 Wrapper 패턴으로 불리기도 한다.
- Adapter 패턴은 두가지로 나뉜다.
    - 클래스에 의한 Adapter 패턴 (상속을 사용한 Adapter 패턴)
    - 인스턴스에 의한 Adapter 패턴 (위임을 사용한 Adapter 패턴)


어댑터 패턴은 기존 클래스의 소스코드를 수정해서 인터페이스에 맞추는 작업보다는 __기존 클래스의 소스코드의 수정 을 전혀 하지 않고__, 타겟 인터페이스에 맞춰 동작을 가능하게 하는것이다.

즉, 기존 클래스의 명세(사양)만 알면 언제든지 새로운 클래스도 작성할 수 있게된다.
    

### 2. 어댑터 패턴의 장점
- 관계가 없는 인터페이스 간 같이 사용이 가능해진다.
- 프로그램 검사에 용이하다.
- 클래스 재활용성이 증가한다.


### 3. 어댑터 패턴의 사용 예
메소드가 피요하면 그것을 프로그래밍 하면 되지 왜 Adapter라는 것을 사용할까?

우리는 언제나 처음부터 프로그래밍을 할 수는 없다.
이미 존재하고 있는 클래스를 이용하는 경우도 굉장히 많기 때문이다.
특히 그 클래스가 충분한 테스트를 받아 버그가 적고, 지금까지 사용된 실적이 있다면 어떻게든 그 클래스를 부품으로 재사용하려할 것이다.

어댑터 패턴은 기존 클래스를 개조하여 필요한 클래스를 만든다.
    - 이 패턴으로 필요한 메소드를 발빠르게 만들수 있다.
    - 만약 버그가 발생해도 안정화된 기존 클래스에는 버그가 없으므로 어댑터 역할의 클래스를 중심적으로 조사하면 되며, 프로그램 검사가 쉬워진다.


### 4. 예제 코드

__MediaPlayer.java__
```java
public interface MediaPlayer {
    void play(String filename);
}
```
MediaPlayer 는 play 메소드

__MediaPackage.java__
```java
public interface MediaPackage {
    void playFile(String filename);
}
```
MediaPackage 는 playFile 메소드


__MP3.java__
```java
public class MP3 implements MediaPlayer {
    @Override
    public void play(String filename) {
        System.out.println("play mp3" + filename);
    }
}
```

__MP4.java__
```java
public class MP4 implements MediaPackage {
    @Override
    public void playFile(String filename) {
        System.out.println("play mp3" + filename);
    }
}
```

__MKV.java__
```java
public class MKV implements MediaPackage {
    @Override
    public void playFile(String filename) {
        System.out.println("play mp3" + filename);
    }
}
```

MP4, MKV 도 play로 실행시킬 방법이 없을까??

__Adapter__를 사용

FormatAdapter.java
```java
public class FormatAdapter implements Mediaplayer {
    private MediaPackage mediaPackage;
    
    public FormatAdapter (MediaPackage mediaPackage) {
        this.mediaPackage = mediaPackage;
    }
    
    @Override 
    public void play(String filename) {
        System.out.println("using Adapter --> ");
        mediaPackage.playFile(filename);
    }
}
```

Main.java
```java
public class Main {
    public static void main (String[] args) {
        MediaPlayer player = new MP3();
        player.play("mp3");
        
        player = new FormatAdapter(new MP4());
        player.play("file.mp4");
        
        // 같은 방식으로 사용이 가능해졌다.
        
    }
}

```