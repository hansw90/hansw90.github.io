---
title: "자바 디자인 패턴 (5). 플라이웨이트 패턴 "
date: 2021-01-02 22:12
categories: JAVA
toc: true
toc_sticky: true
---

2021 첫 포스트, 디자인 패턴중 Facade 패턴, Adapter 패턴, Decorator 패턴과 같은 __구조 패턴__ 인 플라이웨이트 패턴에 대해 알아보자,

### 0. 구조 패턴 (Structural Pattern)
구조 패턴이란 작은 클래스들을 상속과 합성을 이용하여 더 큰 클래스를 생성해가는 패턴이다.
이패턴을 상요하면 서로 __독립__ 적으로 개발한 클래스 라이브러리들을 하나인것 처럼 사용할 수 있다. 또한 여러여러 인터페이스를 합성하여 서로 다른 인터페이스들의 통일된 추상을 제공한다.

구조 패턴의 중요한 중요한 포인트는 인터페이스나 구현을 복합하는 것이 아니라 객체를 합성하는 방법을 제공한다.
컴파일 단계에서가 아닌 런타임 단계에서 복합 방법이나 대상을 변경할 수 있다는 유연성을 갖고 있다는 것을 뜻한다.

### 1. 플라이웨이트 패턴

GoF에서의 플라이웨이트 패턴
```
공유(Sharing)을 통하여 대량의 객체들을 효과적으로 지원하는 방법
```

플라이웨이트 패턴은 비용이 큰 자원을 공통으로 사용할 수 있도록 만드는 패턴이다.
자원에 대한 비용은 크게 두가지로 나뉜다,

1. __중복 생성될 가능성 이 높은__ 경우.
    - 중복 생성될 가능성이 높다는 것은 동일한 자원이 자주 사용될 가능성이 매우 높다는 것을 의미한다. 이러한 자원은 공통 자원 형태로 관리해 주는 편이 좋다.

2. __자원 생성 비용은 큰데 사용 빈도가 낮은__ 경우.
    - 이런 자원을 항상 미리 생성해 두는 것은 낭비이기 때문에 요청이 있을 떄에 생성하여 제공해주는 방벙을 사용한다.

이 두 목적을 위해 플라이웨이트 패턴은 자원 생성과 제공을 책임진다.
자원의 생성을 담당하는 Factory 역할과 관리 역할을 분리하는 것이 좋을수는 있으나, 일반적으로는 두 역할의 크기가 그리 않아 하나의 클래스가 담당하도록 구현한다.

### 2. 플라이웨이트 패턴 장점 

1. 많은 객체를 만들 때 성능을 향상시킬 수 있다.
2. 많은 객체를 만들 때 메모리를 아낄수 있다.
3. state pattern과 쉽게 결합이 가능하다.

### 3. 플라이웨이트 패턴 단점
1. 특정 인스턴스의 공유 컴포넌트를 다르게 행동하게 하는것이 불가하다.

### 4. 플라이웨이트 패턴의 사용 예
1. 워트 프로세서에서의 문자들의 그래픽적 표현
2. jdk java.lang.String
   (두개 이상의 코드에 정의 되있으면 스)
3. jdk java.lang.Integer#valueOf(boolean) 등등 자바의 모든 래퍼 클래스의 valueOf() 메소드
   (입력값이 캐시 매모리에 있는지 확인후 없으면 객체를 생성 하는 방법)
   (이게 Effective java에서도 나온 new 가 아닌 valueOf 메소드를 통해 인스턴스를 생성하는것이 더 효율적인 이유이다.)
   
### 5. 플라이웨이트 패턴 예제

단순 String 예제
```java
public class Main {

    public static void main(String[] args) {
        String s1 = new String("한승우");
        String s2 = new String("한승우");
        String s3 = "한승우";
        String s4 = "한승우";

        System.out.println(s1 == s2); // false
        System.out.println(s3 ==  s4); // true
    }
}
```


+ 예제 도형만들기 
shape interface
```java
import java.awt.*;

public interface Shape {
    public void draw(Graphics g, int x, int y, int width, int height, Color color);
}
```
Line.class, Oval.class
```java

public class Line implements Shape{

    public Line() {
        System.out.println("새로운 선 객체운 생성중입니다.");

        // adding time delay
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void draw(Graphics line, int x, int y, int width, int height, Color color) {
        line.setColor(color);
        line.drawLine(x, y, width, height);
    }
}



public class Oval implements Shape{

   private boolean fill;

   public Oval(boolean fill) {
      this.fill = fill;
      System.out.println("새로운 도형 객체가 생성중입니다.");
      try {
         Thread.sleep(2000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void draw(Graphics g, int x, int y, int width, int height, Color color) {
      g.setColor(color);
      g.drawOval(x,y,width,height);
      if(fill) {
         g.fillOval(x,y,width,height);
      }
   }
}
```
ShapeFactory.class
```java
public class ShapeFactory {
    private static final HashMap<ShapeType, Shape> shapes = new HashMap<ShapeType, Shape>();

    public static Shape getShape(ShapeType type) {
        Shape shapeImpl = shapes.get(type);
        if (shapeImpl == null) {
            if(type.equals(ShapeType.OVAL_FILL)) {
                shapeImpl = new Oval(true);
            } else if (type.equals(ShapeType.LINE)) {
                shapeImpl = new Line();
            }
        }
        shapes.put(type, shapeImpl);

        return shapeImpl;
    }


    public static enum ShapeType {
        OVAL_FILL, OVAL_NOFILL, LINE;
    }
}
```

Main.class
```java

public class DrawingClient extends JFrame{

        private static final long serialVersionUID = -1350200437285282550L;
        private final int WIDTH;
        private final int HEIGHT;

        private static final ShapeFactory.ShapeType shapes[] = { ShapeFactory.ShapeType.LINE, ShapeFactory.ShapeType.OVAL_FILL };

        private static final Color colors[] = { Color.RED, Color.GREEN, Color.YELLOW };

        public DrawingClient(int width, int height) {
            this.WIDTH = width;
            this.HEIGHT = height;
            Container contentPane = getContentPane();

            JButton startButton = new JButton("Draw");
            final JPanel panel = new JPanel();

            contentPane.add(panel, BorderLayout.CENTER);
            contentPane.add(startButton, BorderLayout.SOUTH);
            setSize(WIDTH, HEIGHT);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);

            startButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Graphics g = panel.getGraphics();
                    for (int i = 0; i < 20; ++i) {
                        Shape shape = ShapeFactory.getShape(getRandomShape());
                        shape.draw(g, getRandomX(), getRandomY(), getRandomWidth(),
                                getRandomHeight(), getRandomColor());
                    }
                }
            });
        }

        private ShapeFactory.ShapeType getRandomShape() {
            return shapes[(int) (Math.random() * shapes.length)];
        }

        private int getRandomX() {
            return (int) (Math.random() * WIDTH);
        }

        private int getRandomY() {
            return (int) (Math.random() * HEIGHT);
        }

        private int getRandomWidth() {
            return (int) (Math.random() * (WIDTH / 10));
        }

        private int getRandomHeight() {
            return (int) (Math.random() * (HEIGHT / 10));
        }

        private Color getRandomColor() {
            return colors[(int) (Math.random() * colors.length)];
        }

        public static void main(String[] args) {
            DrawingClient drawing = new DrawingClient(500,600);
        }
}
```

여기서 중요한건 객체 생성 console이 2개 이하로 나온다는 점이다.
