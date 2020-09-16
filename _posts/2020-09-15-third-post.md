
---
title: "오늘의 삽질 2. 지금까지 내가 하고 있던 일과, 대용량 데이터 synchronization 작업"
date: 2020-09-14- 22:54:00 -0000
categories: DB

---


음, 내가 벌써 회사에 와서 일한지 한달이 조금 넘었다,,  
물론 중간 코로나로 인하여 재택등으로 정신이 없었지만,,  

처음와서 받은 일은 대용량 OracleDB data를 받아 MariaDB로 migration하는 작업이였다.  

물론 일반적인 환경이라면, 선택지가 많겠지만, 
이번경우엔 oracleDB Server 환경에서 get할수 있는 port를 열어주면 요청을 통해 데이터를 가져오는 java api를 직접 만드는 프로젝트였다.

단순히 select *을 통해 가져올수 있다면 일주일만 있었어도 충분했겠지만,

데테이블에 이터가 수억 수십억개,, 이걸 테이블당 row by row로 넣자니 어떤테이블은 500시간이 넘게 걸리는 데이터들이였다..

(아 물론 mybatis foreach문을 통해 bulk insert로 처리하였다. 효율이 10배이상이다.)  

또한 OracleDB가 아닌 mariaDB에서 이렇게 큰 데이터를 관리위해 일정한 크기로 [샤딩](http://wiki.hash.kr/index.php/%EC%83%A4%EB%94%A9)작업을 하였다.

모쪼록,, migration작업은 이렇게 완료하였지만, 이 데이터를 이제 일정시간 마다 동기화를 해야하는 문제가 발생하였다,,

Unit Test하기도 빡센 상황,,, ㅠㅠ 지금 아직까지도 내가 하고 있는 일이다.

----
이제 대용량 본격적으로 동기화에 대해서 얘기를 해보자,,

오라클이나 다른DB에도 다양한 써드파티 유틸리티들이 존재한다.그중 유명한것은 Oracle Stream과 GoldenGate
난 물론 위에서 말한것 처럼 이러한 유틸리티 라이브러리들은 사용할수 없는 환경이였다..
- 이건 참조용 [(오라클 데이터의 동기화를 위한 10가지 방볍)](https://blog.devart.com/ten-ways-to-synchronize-oracle-table-data.html)

특히나,, 아래의 경우들같이.
1. 데용량 데이터에서 어떠한 autowired 된 column이나 insert된 date가 기록된 column이 없다면?
마지막으로 업데이트된 컬럼을 찾기란 쉽지 않을것 입니다....  하...
+위의 경우에 추가로 원본 테이블에 물리적인 삭졔(Delete)가 발생하고 변동자료에 삭제된 데이터를 표시하여 제공하지 않는경우...
2. 소스 시스템에서 변동자료 추출 시 변동 레코드가 누락된 경우
3. 타겟 시스템에서 변동자료 중 일부 처리를 누락한 경우
4. 데이터 변동 순서와 처리순서가 바뀐 경우

위와같은 상황이 발생하였을땐 차선 해결법으로는 정기적으로 FULL 데이터를 수신하여 반영하는 것이라고 한다.
평소에는 일별이나, AUTOWIRED를 통한 업데이트를 진행해주다 Quartz의 trigger값등을 조정하여 특정날 모든 데이터를 전부 업데이트 해준다 생각하면 될것 같다

데이터의 중요도가 작고(트랜잭션이 적다고 생각하면 될까요?) 변동데이터를 추적하기 힘든 환경이라면 위와 같은 방법을 적용하여 주기적으로 동기화 작업을 처리하는것도 생각해볼법한 방법인것 같다.

아래선 이러한 FULL DATA동기화 방법을 예시를 들어 알아보도록 하자.

#### FULL  데이터 동기화 방법
FULL 데이터를 수신하여 동기화를 하는 방법은 처리방법과 성능에 대한 고민을 조금 해봐야한다. 소량의 데이터라면 크게 고민하지 않아도 되겠지만, (17만로우 정도의 테이블을 유닛테스트로 진행하였을때 10초도 안걸렸던거 같다) 처음에 썼던 100만이상 천만 억건의 로우의 테이블이라면 이러한 방법은 고려해 봐야할것 같다.  
업무 요건이 온라인 처리가 필요한 환경이라면 (Truncate & Insert) 방식이나 CTAS & Rename 같은 방법은 적절하지 못하다.

### 1. Truncate & Insert 방식
~~~
SQL> DELETE FROM 원본TABLE;
SQL> INSERT INTO 원본Table SLECET * FROM 복사TABLE;
SQL> DELETE FROM 복사TABLE;
~~~


### 2. CTAS & Rename 방법
~~~
SQL> CREATE TABLE 원본_NEW AS SELECT * FROM 복사TABLE;
SQL> ALTER TABLE 원본 RENAME TO 원본_OLD;
SQL> ALTER TABLE 원본_NEW RENAME TO 원본
~~~
