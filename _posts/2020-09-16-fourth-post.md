---
title: "오늘의 삽질 3. spring scheduler Quartz에 대해 알아보자"
date: 2020-09-16 23:40:00 -0000 
categories: spring

---
오늘 본 에러만 몇개인지,, 
실력이 없으니 글 쓸게 많다.. 좋은건가?? 


그렇지만 오늘은 에러를 다루지 않고 Quartz job 에 대해 알아보려 한다.
지금 하는일이 동기화 작업이다 보니, 시간 또는 주기적으로 JOB을 실행해야될 필요가 있었다. 
그렇다고 SLEEP을 해서 하면 팀장님한테 혼날것 같아서, Spring Quartz를 이용해 만들어 보기로 결정하였다. 

#### 참고 문서 
[## Scheduling in Spring with Quartz Example 예제 코드](https://github.com/eugenp/tutorials/tree/master/spring-quartz)  
[# Scheduling in Spring with Quartz 코드 설명](https://www.baeldung.com/spring-quartz-schedule)

