# 9. 중첩 클래스, 내부 클래스2
- 1. 지역 클래스
- 지역 클래스도 지역 변수와 같이 코드 블럭 안에서 정의된다.
- 지역 클래스는 자신이 속한 메서드 (바깥 메서드)의 지역 변수를 사용할 수 있다.
- 단, 그 지역 변수는 반드시 final 또는 effectively final(사실상 final) 이어야 한다.
- 즉, 지역 클래스에서 접근하는 지역 변수는 값이 한 번만 할당되고 변경되지 않는 값이어야 한다.
```java
public void localOuterMethod() {
    int localVar = 0;  //사실상 final인 지역 변수여야 읽기 가능
    class Local {
        Local() {
            //localVar = 2; //컴파일 에러
            System.out.println(localVar); //가능
        }
    }
    Local local = new Local();
}
//지역 클래스 안에서 값을 바꾸려고 해서 문제가 된다.
```
```java
public class LocalOuter2 {
    public void localOuter2Method() {
        int localVar = 0;
        class Local {
            Local() {
                //System.out.println(localVar); //컴파일 에러
            }
        }
        localVar = 1;
        Local local = new Local();
    }
}
//사실상 final 아니라서 읽기도 안 된다.
```
- 2. JVM 내부적으로 지역 변수를 직접 캡처(capture)하는 게 아니라,
- 복사본을 생성해서 지역 클래스 객체에 넘기는 방식으로 동작한다.
- 즉, 원래의 localVar과는 다른 메모리에 저장돼서 일종의 읽기 전용처럼 취급해야 한다.
- 그래서 지역 클래스에서는 그 지역 변수의 값만 읽을 수 있고, 변경은 불가능하다.
- 값을 바꾸고 싶다면, 지역 변수를 직접 쓰는 게 아니라 필드나 배열 같은 참조형을 활용한다.
```java
public class LocalOuter {
    public void localOuterMethod() {
        final int[] localVar = {0}; //참조형 배열
        class Local {
            Local() {
                localVar[0] = 2; //값 변경 가능
            }
        }
        Local local = new Local();
        System.out.println(localVar[0]); //2
    }
}
```
```java
public class LocalOuter2 {
    public void localOuter2Method() {
        int localVar = 0;
        int finalLocalVar = localVar;
        class Local {
            Local() {
                //System.out.println(localVar); //불가
                System.out.println(finalLocalVar); //가능
            }
        }
        localVar = 1;
        Local local = new Local();
    }
}
//내부 클래스는 바깥 메서드 지역 변수의 복사본을 캡처한다. (쓰기 불가)
//finalLocalVar는 선언할 때 localVar의 값을 복사해서 사실상 final이다.
//final이랑 사실상 final은 복사본 캡처가 가능하다. (읽기 가능)
//localVar은 사실상 final이 아니라서 안 된다. (읽기 불가)
```
- 3. static nested나 inner class
- 바깥 메서드를 호출하는지 아닌지를 클래스/인스턴스로 구분할 수 있어도
- 바깥 메서드의 지역 변수에 직접 접근은 안 된다. (지역 변수는 메서드 호출 시 생성)
- 즉, 지역 변수에 접근할 수 있는 건 지역 클래스다.
```java
class InnerOuter {
    public static int outerClassField = 0;
    public int outerInstanceField = 1;
    class Inner {
        public int innerInstanceField = 2;
        void innerMethod() {
            int innerLocalField = 3;
            //바깥 클래스의 클래스 변수 쓰기 가능
            InnerOuter.outerClassField = 10;
            InnerOuter.this.outerClassField = 11;
            //this.outerClassField = 12;
            outerClassField = 13;
            //바깥 클래스의 인스턴스 변수 쓰기 가능
            //InnerOuter.outerInsInstanceField = 20;
            InnerOuter.this.outerInstanceField = 21;
            //this.outerInstanceField = 22;
            outerInstanceField = 23;
            //내부 클래스의 인스턴스 변수 쓰기 가능
            //InnerOuter.Inner.innerInstanceField = 30;
            InnerOuter.Inner.this.innerInstanceField = 31;
            this.innerInstanceField = 32;
            innerInstanceField = 33;
            //내부 메서드의 지역 변수 쓰기 가능
            //this.innerLocalField = 40;
            innerLocalField = 41;
            //바깥 메서드 호출만 가능
            outerMethodOne(1);
            outerMethodTwo();
        }
    }
    void outerMethodOne(int i) {
        int outerLocalVarOne = i;
    }
    void outerMethodTwo() {
        final int outerLocalVarTwo = 0;
    }
}
```
- 4. 지역 변수를 캡처하는 이유
- 메서드에서 선언된 지역 변수는 스택 프레임(stack frame)에 저장
- 메서드가 끝나면 스택 프레임이 사라짐
- 지역 클래스(또는 람다)로 만든 객체는 힙(heap)에 저장
- 메서드가 끝나도 객체는 사라지지 않음
- 컴파일러는 내부 클래스에서 참조한 지역 변수를 복사해서
- 내부 클래스 인스턴스의 필드로 저장 = 캡처
- 지역 변수가 계속 바뀐다면 일관성 문제 생겨 사실상 final만 허용
- 반면에, 인스턴스 필드나 static 필드는 힙/메서드 영역에 저장돼서
- 내부 클래스가 직접 참조 가능 → 캡처가 필요 없고 final 제약도 없음
- 5. 지역 클래스 스코프
- 지역 클래스는 선언된 메서드 내부에서만 유효하다.
- 지역 클래스는 해당 메서드 안에서만 사용할 수 있다.
- 메서드 외부에서는 이름조차 알 수 없다.
- Local은 process 메서드 내부에만 존재하고,
- 메서드 시그니처는 클래스 외부에서 보이기 때문이다.
- 즉, 바깥 클래스에서 접근 가능한 타입이 아니어서 반환 타입으로 지정할 수 없다.
```java
public class LocalCapture {
    private int outVar = 0;
    public Object process(int paramVar) {
        int localVar = 1;
        class Local {
            int value = 2;
            public void print() {
                System.out.println(value);
                System.out.println(localVar);
                System.out.println(paramVar);
                System.out.println(outVar);
            }
        }
        Local local = new Local();
        return local;
    }
    //컴파일러가 Local이라는 타입을 반환 타입으로 인식할 수 없다.
    public void outMethod() {
        //Local local = new Local(); //Local에 접근 불가
        Object o = process(3); //가능
        //o.print(); //Object라서 안 됨
    }
}
```
- Object로 받아서 바로 사용할 수 없다.
- 캐스팅하려면 인터페이스나 상속을 통해 타입을 접근 가능하게 한다.
```java
public class LocalCapture2 {
    public Printable process(int paramVar) {
        int localVar = 1;
        class Local implements Printable {
            int value = 2;
            public void print() {
                System.out.println(value);
                System.out.println(localVar);
                System.out.println(paramVar);
            }
        }
        Printable local = new Local();
        return local;
    }
    public void outMethod() {
        Object o = process(3);
        if (o instanceof Printable) {
            ((Printable) o).print(); //안전하게 호출 가능
        }
    }
}
```
- 익명 클래스로 바꾸면 이렇다. (outMethod()는 같다.)
```java
public Printable process(int paramVar) {
    int localVar = 1;
    return new Printable() {
        public void print() {
            System.out.println(localVar);
            System.out.println(paramVar);
        }
    };
}
```
- 6. 익명 클래스 (Anonymous Class)
- 이름이 없는 클래스, 지역 클래스의 일종이다.
- 클래스를 정의하면서 동시에 인스턴스를 생성한다.
- 주로 일회성으로 객체를 생성해 사용할 때 쓰인다.
- 인터페이스나 추상 클래스를 구현할 때 자주 사용된다.
- 일반 클래스를 상속해서 사용할 수도 있다.
```java
class MyClass {
    void greet() {
        System.out.println("Hello");
    }
}
MyClass obj = new MyClass() {
    @Override
    void greet() {
        System.out.println("Hi there!");
    }
};
obj.greet(); //Hi there!
```
- 7. 익명 클래스 기본 문법
```java
인터페이스 또는 클래스 변수 = new 인터페이스 또는 클래스() {
    //메서드 구현 또는 오버라이딩
};
```
- 예제 1: 인터페이스 구현
- new Runnable()이 Runnable 인터페이스를 생성하는 것처럼 보이지만,
- Runnable 인터페이스를 구현한 익명 클래스를 생성하는 것이다.
- 즉, new 다음에 상속(구현)할 클래스를 입력하고 {...}로 선언한다.
```java
Runnable runner = new Runnable() {
    @Override
    public void run() {
        System.out.println("익명 클래스에서 실행 중");
    }
};
runner.run();
```
- 예제 2: 추상 클래스 확장
```java
abstract class Animal {
    abstract void sound();
}
Animal dog = new Animal() {
    @Override
    void sound() {
        System.out.println("멍멍!");
    }
};
dog.sound();
```
- 예제 3: 이벤트 리스너에 사용
```java
button.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("버튼 클릭됨!");
    }
});
//GUI 프로그래밍에서 이벤트 처리 시 자주 사용
```
- 8. 익명 클래스는 한 번만 사용할 수 있고, 재사용 불가
- 클래스 정의 자체를 재사용할 수 없다는 의미
- 익명 클래스로 만든 객체 자체는 변수에 할당해서 여러 번 사용 가능
```java
//클래스 재사용 불가
Runnable r1 = new Runnable() {
    public void run() {
        System.out.println("익명 클래스 실행");
    }
};
//r1과 r2는 서로 다른 익명 클래스 인스턴스
Runnable r2 = new Runnable() {
    public void run() {
        System.out.println("익명 클래스 실행");
    }
};
```
```java
//객체 재사용 가능
Runnable r = new Runnable() {
    public void run() {
        System.out.println("익명 클래스 실행");
    }
};
r.run();  //첫 번째 사용
r.run();  //두 번째 사용
```
- 9. 익명 클래스도 결국 JVM이 내부적으로 이름을 붙여서 컴파일
- Outer$1 같은 식으로 이름이 붙고, Class 객체로도 접근할 수 있다.
- 하지만, 리플렉션으로 다시 생성하기는 어렵다.
- 익명 클래스는 외부 클래스의 컨텍스트에 강하게 의존해서
- 내부 상태나 캡처된 변수에 따라 동작한다.
- 리플렉션으로 생성해도 원래와 동일한 동작을 보장할 수 없다.
- 익명 클래스는 생성자가 명시적으로 정의되지 않는다.
- newInstance()가 실패하거나 IllegalAccessException이 발생할 수 있다.
- 익명 클래스는 이름이 없고, 코드상에서 직접 참조 불가하다.
```java
//일반 클래스는 이름으로 참조 가능
class MyRunnable implements Runnable {
    public void run() {
        System.out.println("Hello");
    }
}
//MyRunnable이라는 이름이 있으니까:
//다른 곳에서 new MyRunnable()로 객체 생성 가능
//MyRunnable.class로 클래스 정보 참조 가능
//다른 클래스에서 extends MyRunnable로 상속 가능
```
```java
//익명 클래스는 이름이 없어서 참조 불가
Runnable r = new Runnable() {
    public void run() {
        System.out.println("익명 클래스");
    }
};
//익명 클래스는 이 코드 블록 안에서만 존재하고,
//자바 코드에서 익명클래스이름.class처럼 직접 참조할 수 있는 이름이 없다.
//new 익명클래스() → 불가능 (이름이 없으니까)
//익명클래스.class → 불가능
//extends 익명클래스 → 불가능
//리플렉션으로는 접근 가능
Class<?> clazz = r.getClass();
System.out.println(clazz.getName()); //예: com.example.Outer$1
//컴파일러가 자동으로 만든 이름
//자바 코드에서 직접 쓸 수 있는 이름은 아니다.
```
- 10. 익명 클래스 내부에서 생성자x, 초기화는 가능
- 이름이 없어서 생성자 선언 불가 (기본 생성자만 사용된다.)
```java
Runnable r = new Runnable() {
    //public ???() { ... } ← 클래스 이름이 없어서 불가능
    public void run() {
        System.out.println("익명 클래스 실행");
    }
};
```
- 생성자 대신 인스턴스 초기화 블록을 사용한다.
```java
Runnable r = new Runnable() {
    {
        System.out.println("초기화 블록 실행됨");
    }
    //중괄호 { ... }로 감싼 블록이 객체가 생성될 때 자동으로 실행
    //생성자처럼 초기화 작업을 수행할 수 있다.
    public void run() {
        System.out.println("익명 클래스 실행");
    }
};
//초기화 블록은 일반 클래스에서도 가능
```
- 11. 익명 클래스 활용
- 변하는 부분을 변하지 않는 부분과 분리한다.
- 메서드에 변하는 부분을 외부에서 넘기도록 한다.
- 메서드 재사용성이 높아진다.
- 처음에는 변하는 문자열 데이터를 외부에서 전달하도록 했다.
- 이번에는 변하는 코드 조각을 전달해본다.
```java
public class After {
    public static void hello(String s, AfterInterface a) {
        System.out.println(s + " 시작");
        a.run();
        System.out.println(s + " 종료");
    }
    static class Dice implements AfterInterface {
        @Override
        public void run() {
            System.out.println(new Random().nextInt(6) + 1);
        }
    }
    static class Sum implements AfterInterface {
        @Override
        public void run() {
            for (int i=1; i<=3; i++) {
                System.out.println(i);
            }
        }
    }
    public static void main(String[] args) {
        hello("Dice", new Dice());
        hello("Sum", new Sum());
    }
}
//익명 클래스 아닌, 명명된 클래스 사용 중
```
```java
public class Anon {
    public static void hello(String s, AfterInterface a) {
        System.out.println(s + " 시작");
        a.run();
        System.out.println(s + " 종료");
    }
    public static void main(String[] args) {
        hello("Dice", new AfterInterface() {
            @Override
            public void run() {
                System.out.println(new Random().nextInt(6) + 1);
            }
        });
        hello("Sum", new AfterInterface() {
            @Override
            public void run() {
                for (int i = 1; i <= 3; i++) {
                    System.out.println(i);
                }
            }
        });
    }
}
//익명 클래스 사용
```
- 12. 익명 클래스는 구현 > 상속
- 한 번만 사용할 클래스를 간단히 정의할 때 쓴다.
- 상속은 필드랑 메서드 많아져서, 익명 클래스와는 목적이 조금 다르다.
- 추상 클래스를 상속하면서 익명 클래스를 만드는 건 목적에 가깝다.
- 추상 클래스가 인터페이스랑 비슷하니까..
```java
abstract class Animal {
    abstract void sound();
}
public class Main {
    public static void main(String[] args) {
        Animal dog = new Animal() {
            @Override
            void sound() {
                System.out.println("멍멍!");
            }
        };
        dog.sound(); //멍멍!
    }
}
```
- 13. 람다
- 기본형 타입이나, 참조형 타입=인스턴스를 메서드에 인수로 전달할 수 있다.
- 코드 조각을 전달하기 위해, 인스턴스를 만들어야 할까? 메서드만 전달하면 안 될까?
- 람다로 된다. 람다식은 함수형 인터페이스를 기반으로 동작한다.
- 메서드 하나만 있는 인터페이스가 있어야 람다식으로 표현할 수 있다.
```java
@FunctionalInterface
interface OneInterface {
    void run();
}
public class Lambda {
    public static void hello(String s, OneInterface o) {
        System.out.println(s + " 시작");
        o.run();
        System.out.println(s + " 종료");
    }
    public static void main(String[] args) {
        hello("Dice", () -> {
            System.out.println(new Random().nextInt(6) + 1);
        });
        hello("Sum", () -> {
            for (int i = 1; i <= 3; i++) { System.out.println(i); }
        });
    }
}
```
- 14. 함수형 인터페이스에 메서드가 하나만 있어야 하는 이유
- 람다식은 "하나의 동작"을 표현한다.
- 람다식은 하나의 함수처럼 동작하는 객체를 만들기 위한 문법이다.  
- () -> System.out.println("Hello")는 "실행하면 Hello를 출력하는 동작"을 나타낸다.
- 이걸 객체로 만들려면, 그 람다식이 어떤 인터페이스의 메서드를 구현하는지 알아야 한다.  
- 그래서 딱 하나의 추상 메서드만 있는 인터페이스를 람다식의 대상이라고 정했다.
- 람다식은 어떤 메서드를 구현하는지 명확해야 한다.
- 메서드가 여러 개면, 어떤 걸 구현하는지 컴파일러가 헷갈림
- 람다식은 결국 함수형 프로그래밍의 개념을 Java에 도입한 것이다.
- 함수는 하나의 입력 → 하나의 출력이 기본이니까, 인터페이스도 그에 맞춰야 한다.
- 예시: 함수형 인터페이스
```java
@FunctionalInterface
interface Printer {
    void print(String s);
}
```
- 이 인터페이스는 print(String s) 하나만 있어서, 람다식으로 이렇게 쓸 수 있다.
```java
Printer p = s -> System.out.println(s);
p.print("안녕하세요!");
```
- 만약 메서드가 두 개라면? 람다식으로는 사용할 수 없다.
- s -> System.out.println(s)가 print인지 getStatus인지 알 수 없어서다.
```java
interface OtherPrinter {
    void print(String s);
    void getStatus();
}
```
- 람다식은 "이 코드 블록을 어떤 메서드에 연결할지"를 컴파일러가 알아야 한다.
```java
OneInterface o = () -> System.out.println("Hello");
```
- o.run();은 메서드 호출 시점에 실행된다.
- 그런데, 람다식은 익명 클래스처럼 런타임에 동적으로 연결되는 게 아니라
- 컴파일 시점에 타입이 결정되고 메서드가 연결된다. (정적 타입 시스템)
- 하나의 추상 메서드만 있어야 컴파일러가 람다식이 이 메서드를 구현한다고 판단한다.
- @FunctionalInterface를 붙이는 건 명시적으로 선언하는 어노테이션이다.
- 붙이지 않아도 추상 메서드가 2개 이상인데 람다식을 쓰면 컴파일 오류 난다.
```java
public class Test {
    public static void hello(String s, TwoInterface t) {
        System.out.println(s + " 시작");
        t.run();
        System.out.println(s + " 종료");
    }
    public static void main(String[] args) {
        /*
        hello("Dice", () -> {
            System.out.println(new Random().nextInt(6) + 1);
        });
        hello("Sum", () -> {
            for (int i = 1; i <= 3; i++) { System.out.println(i); }
        });
        */
        //java: incompatible types: nested2.lambda.TwoInterface is not a functional interface
        //multiple non-overriding abstract methods found in interface nested2.lambda.TwoInterface
    }
}
```
# 10. 예외 처리1 - 이론