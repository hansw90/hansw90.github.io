---
title: "오늘의 삽질 2. 지금까지 내가 하고 있던 일과, 대용량 데이터 synchronization 작업"
date: 2020-09-14- 22:54:00 -0000
categories: STRING.XML안에 있는 

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

