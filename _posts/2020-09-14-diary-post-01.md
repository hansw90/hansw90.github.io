---
title: "오늘의 삽질 1. xml Excape 문자 처리 (200914)"
date: 2020-09-14- 22:54:00 -0000
categories: diary

---
제대로 된?? 첫글을 적어본다,,
오늘의 삽질은 mybatis-config.XML 
db.properties.url을 설정하는 부분에서 두개 이상의 옵션을 줄때 에러가 발생하였다
사실 보안상의 이유로 xml안에 db의 정보를 바로 적는것 자체가 에러이지만 단순 테스트를 돌리려 만들었던 모델이라 에러를 만나고 당황하였다,

~~~xml
<properties resource="db.properties"></properties>
<!-- 원래는 properties 태그를 사용하여 보안상 비공유 되야할 소스들을 분리한다. resource 안에 소스가 들어있는 경로 를 설정해준다.   -->

<!-- db.properties의 경로를통해 아래와 같이 가져오는것이 맞지만, -->
<property name="driver" value="${db.driver}"/>
<property name="url" value="${db.url}"/>
<property name="username" value="${db.username}"/>
<property name="password" value="${db.password}"/>

<!-- db.properties의 경로를통해 아래와 같이 가져오는것이 맞지만, 저는 아래와 같이 작성하였습니다. -->
<property name="driver" value="com.mysql.jdbc.Driver"/>
<property name="url" value="jdbc:mysql://URL주소:포트번호/DB명?allowMultiQueries=true&useAffectedRows=true "/>
<property name="username" value="유저네임"/>
<property name="password" value="패스워드"/>
~~~

그런데 여기서 이상한 애러가 발생한다.
~~~xml
<property name="url" value="jdbc:mysql://URL주소:포트번호/DB명?allowMultiQueries=true&useAffectedRows=true "/>
~~~

&&&& __and!!__ 이 부분이 에러의 주 원인이였다.
xml에선 특수문자를 Excape 문자처리를 해줘야 한다고 한다.

처리 방법은 아래와 같다.
XML 에서 엔터나 & 와 같은 특수문자를 사용할 때는 아래와 같이 쓴다.
~~~
1. 공백문자.  
수평 탭 : 09  
line-feed : 0A  
carrage-return: 0D  
ASCII space : 90  

2. Escape 문자  
- & : &amp;  
- < : &lt;  
- > : &gt;  
- ‘ : &apos;  
- ” : &quot;  
- 엔터 : &#10;  
~~~
### 해결
~~~xml
<property name="url" value="jdbc:mysql://URL주소:포트번호/DB명?allowMultiQueries=true&amp;useAffectedRows=true "/>
~~~
