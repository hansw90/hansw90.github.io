---
title: "200914 오늘의 삽질 1. xml EXcape 문자 처리"
date: 2020-09-14- 22:54:00 -0000
categories: STRING.XML안에 있는 

---
제대로 된?? 첫글을 적어본다,,
오늘의 삽질은 mybatis-config.XML 
db.properties.url을 설정하는 부분에서 두개 이상의 옵션을 줄때 에러가 발생하였다
사실 보안상의 이유로 xml안에 db의 정보를 바로 적는것 자체가 에러이지만 단순 테스트를 돌리려 만들었던 모델이라 에러를 만나고 당황하였다,

​```mysql
<properties resource="db.properties"></properties>
<!-- 원래는 properties 태그를 사용하여 보안상 비공유 되야할 소스들을 분리한다. resource 안에 소스가 들어있는 경로 를 설정해준다.   -->


​```


Check out the [Jekyll docs][jekyll-docs] for more info on how to get the most out of Jekyll. File all bugs/feature requests at [Jekyll’s GitHub repo][jekyll-gh]. If you have questions, you can ask them on [Jekyll Talk][jekyll-talk].

[jekyll-docs]: https://jekyllrb.com/docs/home
[jekyll-gh]:   https://github.com/jekyll/jekyll
[jekyll-talk]: https://talk.jekyllrb.com/
