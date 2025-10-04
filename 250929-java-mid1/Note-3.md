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
- 컴파일 시점에 타입이 결정되고 메서드가 연결된다.
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
- 15. 자바가 제공하는 함수형 인터페이스
```java
Consumer<T>      //T를 받아서 소비 (void)
Supplier<T>      //T를 생산
Function<T, R>   //T를 받아서 R을 반환
Predicate<T>     //T를 받아서 boolean 반환
//T는 제네릭 타입 파라미터
```
- 사용 방법1: 직접 메서드 만듦 (람다식 직접 전달)
```java
//내가 직접 만든 메서드
public static void processString(String str, Consumer<String> action) {
    System.out.println("처리 시작:");
    action.accept(str);  //람다식 실행
    System.out.println("처리 끝");
}
processString("안녕", s -> System.out.println(s.toUpperCase()));
```
- 사용 방법2: 이미 만들어진 메서드 (람다식 직접 전달)
```java
//자바가 이미 만들어놓은 forEach 메서드
public void forEach(Consumer<T> action) {
    for (T element : this) {
        action.accept(element);  //람다식 실행
    }
}
List<String> names = Arrays.asList("Alice", "Bob");
names.forEach(name -> System.out.println(name));
```
- 16. 함수형 인터페이스 사용 방법 다시
- (1) 람다식 직접 전달
- 한 번만 사용할 때 적합, 재사용 불가
```java
List<String> names = Arrays.asList("Alice", "Bob");
names.forEach(name -> System.out.println(name));
```
- (2) 메서드 참조
- 파라미터를 그대로 전달할 때만 사용 가능, 재사용 불가
```java
List<String> names = Arrays.asList("Alice", "Bob");
names.forEach(name -> System.out.println(name));
```
- (3) 명시적 변수 (람다식)
- 변수명으로 의도 명확히 표현, 재사용 가능
```java
Consumer<String> printer = name -> System.out.println(name);
names.forEach(printer);
```
- (4) 명시적 변수 (메서드 참조)
- 재사용 가능, 간결함
```java
Consumer<String> printer = System.out::println;
names.forEach(printer);
```
- 17. 람다식 관련 명시적 변수를 선언하면..
```java
Consumer<String> printer = name -> System.out.println(name);
printer.accept("안녕");
```
- 1단계: 컴파일 (javac가 하는 일)
- Consumer<String> printer = name -> System.out.println(name);를 변환
```java
//컴파일러가 변환
//A. 람다 본문을 메서드로 빼냄
private static void lambda$main$0(String name) {
    System.out.println(name);  //내가 쓴 로직이 여기 들어감
}
//B. 원래 자리는 특수 명령어로 변경
//invokedynamic "나중에 Consumer 만들 때 lambda$main$0 사용해!"
```
- 결과를 .class 파일에 저장
```java
MyClass.class 파일 안에:
- main() 메서드
- lambda$main$0() 메서드  ← 내가 쓴 로직
- invokedynamic 명령어     ← "Consumer 만들기" 지시서
```
- 2단계: 실행 (JVM이 하는 일)
- Consumer<String> printer = name -> System.out.println(name);가 실행
```java
//JVM이 하는 일
//1. invokedynamic 명령어를 발견
//2. "아, Consumer 객체를 만들어야겠구나"
//3. 즉석에서 클래스를 만듦 (메모리에만)
//(개념적으로 이런 클래스)
class 자동생성클래스 implements Consumer<String> {
    public void accept(String name) {
        lambda$main$0(name);  //← .class에 있는 메서드 호출
    }
}
//4. 이 클래스의 객체를 힙에 생성
printer = new 자동생성클래스();
```
- 3단계: 사용
- printer.accept("안녕");가 실행
```java
1. printer.accept("안녕") 호출
   ↓
2. 자동생성클래스의 accept("안녕") 실행
   ↓
3. lambda$main$0("안녕") 호출
   ↓
4. System.out.println("안녕") 실행
   ↓
5. 콘솔에 "안녕" 출력
```
- 예제 코드
```java
public class MyClass {
    public static void main(String[] args) {
        //===== 이 줄이 실행되면 =====
        Consumer<String> printer = name -> System.out.println(name);
        //1. JVM이 .class 파일에서 lambda$main$0 메서드를 찾음
        //2. 그 메서드를 사용하는 Consumer 클래스를 메모리에 만듦
        //3. 그 클래스의 객체를 힙에 생성
        //4. printer 변수가 그 객체를 가리킴
        //===== 이 줄이 실행되면 =====
        printer.accept("안녕");
        //1. printer가 가리키는 객체의 accept() 호출
        //2. accept() 안에서 lambda$main$0("안녕") 호출
        //3. System.out.println("안녕") 실행
        //4. 콘솔에 "안녕" 출력
    }
    //컴파일러가 자동으로 생성한 메서드 (보이지 않지만 .class에 있음)
    //private static void lambda$main$0(String name) {
    //    System.out.println(name);
    //}
}
```
- 18. invokedynamic의 정체
```java
Consumer<String> printer = name -> System.out.println(name);
```
- 컴파일 후 .class 파일 (바이트코드)
```java
파일: MyClass.class
[메서드들]
1. main() 메서드
   - 코드 시작
   - invokedynamic #1  ← 여기가 핵심
   - 변수에 저장
   - ...
2. lambda$main$0(String) 메서드
   - System.out.println 호출
[invokedynamic 정보 #1]
- 인터페이스: Consumer
- 메서드: accept
- 실제 로직: lambda$main$0을 호출하라
- 부트스트랩: LambdaMetafactory.metafactory
```
- invokedynamic은 "나중에 실행할 때 이렇게 해줘"라는 지시서다.
- 런타임에 일어나는 일 (단계별)
- 상황: 프로그램 실행 중
```java
public static void main(String[] args) {
    Consumer<String> printer = name -> System.out.println(name);
    //                         ↑
    //                    JVM이 여기 도착
}
```
- 1단계: invokedynamic 명령어 발견
```java
JVM: "어? invokedynamic이네?"
JVM: "이건 첫 실행이면 뭔가 만들어야 하는 명령어구나"
JVM: "지시서를 읽어보자..."
- Consumer 인터페이스를 구현하는 클래스를 만들어라
- accept 메서드를 만들어라
- accept 안에서 lambda$main$0을 호출하게 만들어라
```
- 2단계: 부트스트랩 메서드 호출
```java
JVM: "LambdaMetafactory.metafactory를 호출하자"
     (이건 자바가 제공하는 특수 메서드)
LambdaMetafactory: "알겠어! Consumer 클래스 만들어줄게"
```
- 3단계: 클래스 동적 생성 (메모리에만)
```java
//LambdaMetafactory가 메모리에 이런 클래스를 만듦
class MyClass$$Lambda$1 implements Consumer<String> {
    @Override
    public void accept(String name) {
        //여기서 .class 파일에 있는 lambda$main$0을 호출
        MyClass.lambda$main$0(name);
    }
}
//이 클래스는 .class 파일로 저장 안 됨
//오직 메모리(JVM)에만 존재
//실행 중에만 존재하고, 프로그램 끝나면 사라짐
```
- 4단계: 객체 생성
```java
//JVM이 방금 만든 클래스로 객체 생성
Consumer<String> printer = new MyClass$$Lambda$1();
//new MyClass$$Lambda$1()
//printer = [힙의 MyClass$$Lambda$1 객체]
```
- JVM이 어떻게 lambda$main$0을 찾나?
- invokedynamic 지시서에 다 적혀있음
```java
[invokedynamic #1의 정보]
┌────────────────────────────────────┐
│ 부트스트랩 메서드:                  │
│   LambdaMetafactory.metafactory    │
│                                    │
│ 인자들:                             │
│ - 인터페이스: Consumer             │
│ - 메서드 이름: accept              │
│ - 메서드 시그니처: (Object)void    │
│ - 구현 메서드: lambda$main$0       │ ← 여기
│ - 구현 타입: static                │
│ - 구현 클래스: MyClass             │
└────────────────────────────────────┘
```
- 19. 람다의 클래스와 객체는 어디에?
```java
┌─────────────────────────────────────────┐
│          JVM 메모리                      │
├─────────────────────────────────────────┤
│  [Method Area / Metaspace]              │  ← 클래스 정의 저장
│  - 클래스 메타데이터                     │
│  - 메서드 정보                           │
│  - 상수 풀                               │
│  - static 변수                          │
├─────────────────────────────────────────┤
│  [Heap]                                 │  ← 객체 인스턴스 저장
│  - new로 만든 객체들                     │
│  - 배열                                 │
│  - 인스턴스 변수                         │
├─────────────────────────────────────────┤
│  [Stack]                                │  ← 메서드 호출, 지역변수
│  - 메서드 프레임                         │
│  - 지역 변수 (참조값)                    │
│  - 메서드 파라미터                       │
└─────────────────────────────────────────┘
```
- 클래스 정의 → Metaspace (Method Area)
```java
[Metaspace]
┌──────────────────────────────────────┐
│ MyClass$$Lambda$1 클래스 정의         │
│                                      │
│ - 클래스 이름                         │
│ - 구현 인터페이스: Consumer          │
│ - 메서드 정보:                        │
│   * accept(String) 메서드            │
│     → lambda$main$0 호출             │
│ - 바이트코드                         │
└──────────────────────────────────────┘
//MyClass$$Lambda$1 클래스 ← 동적생성
```
- 객체 인스턴스 → Heap
```java
[Heap]
┌──────────────────────────────────────┐
│ MyClass$$Lambda$1 객체               │
│                                      │
│ - 타입 정보 참조 → Metaspace         │
│ - 인스턴스 데이터                     │
└──────────────────────────────────────┘
//MyClass$$Lambda$1 객체 (Consumer 구현체)
```
- 참조 변수 → Stack
```java
[Stack]
┌──────────────────────────────────────┐
│ main() 메서드 프레임                  │
│                                      │
│ printer: [참조값] ──→ Heap의 객체    │
└──────────────────────────────────────┘
```
- 20. 일반 클래스와 람다 클래스 비교
- 일반 클래스
```java
// A.java 작성
public class MyConsumer implements Consumer<String> {
    public void accept(String s) {
        System.out.println(s);
    }
}
// 컴파일: MyConsumer.class 파일 생성
// 실행: 클래스 로딩 시 Metaspace에 적재
.class 파일 (디스크)
    ↓ 클래스 로딩
Metaspace (클래스 정의)
    ↓ new MyConsumer()
Heap (객체 생성)
```
- 람다 클래스
```java
Consumer<String> printer = name -> System.out.println(name);
.class 파일 (디스크)
- lambda$main$0 메서드만 있음
- MyClass$$Lambda$1.class 파일은 없음
        ↓ invokedynamic 실행
Metaspace (클래스 정의 동적 생성)
- MyClass$$Lambda$1 클래스 정의를 즉석에서 만듦
    ↓ 객체 생성
Heap (객체 생성)
```
- 람다 클래스 디렉토리 확인 (컴파일 후)
```bash
# 컴파일
javac MyClass.java
[컴파일러]
"간단한 메서드네. 파일 만들 필요 없어"
"메서드만 추출하고, invokedynamic 넣자"
# 디렉토리 확인
ls -la
MyClass.java         ← 원본 소스
MyClass.class        ← 컴파일 결과
//MyClass$$Lambda$1.class  ← 없음
//람다는 별도의 .class 파일을 만들지 않음
//MyClass.class 안에 lambda$main$0 메서드만 들어있음
//invokedynamic 명령어만 들어있음
```
- 람다 클래스 디렉토리 확인 (런타임 중)
```bash
# 실행
java MyClass
MyClass.java         ← 그대로
MyClass.class        ← 그대로
MyClass$$Lambda$1.class  ← 여전히 없음
//없지만 메모리에는 있다. 파일 없이 메모리에만 동적 생성
[Metaspace]
┌────────────────────────────┐
│ MyClass 클래스              │ ← .class 파일에서 로딩
│ - lambda$main$0() 메서드    │
└────────────────────────────┘
┌────────────────────────────┐
│ MyClass$$Lambda$1 클래스    │ ← 런타임에 동적 생성
│ - accept() 메서드           │
└────────────────────────────┘
```
- 익명 클래스 디렉토리 확인 (컴파일 후)
```java
//익명 클래스
Consumer<String> printer = new Consumer<String>() {
    @Override
    public void accept(String s) {
        System.out.println(s);
    }
};
[컴파일러]
"복잡한 클래스네. 파일로 만들자"
"MyClass$1.class 생성"
//디렉토리
MyClass.java
MyClass.class
MyClass$1.class //생성됨
```
- 익명 클래스 디렉토리 확인 (런타임 중)
```java
디스크:
MyClass.java
MyClass.class
MyClass$1.class //있음
메모리:
[Metaspace]
MyClass$1 클래스 ← .class 파일에서 로딩됨
```
- 익명 클래스는 별도 파일이 필요하다.
- 필드를 가질 수 있고, 여러 메서드를 가질 수 있고,
- 초기화 블록을 가질 수 있고, 상태를 가질 수 있다.
- 람다는 단순한 메서드여서 동적으로 만들 수 있다.
- 필드를 가질 수 없고, 메서드 하나만 가질 수 있고,
- 생성자 가질 수 없고, 상태를 가질 수 없다.
- 21. 익명 클래스와 람다 클래스 비교2
- 타입 시스템: 컴파일 시 클래스 타입 결정되면 정적 타입 시스템이다.
- 람다도, 익명 클래스도 정적 타입 시스템이다.
- 메서드 연결: 컴파일 시 결정되면 정적이다.
- 람다는 정적 메서드 연결 한다.
- 익명 클래스는 런타임에 vtable 조회해서 동적 메서드 연결한다.
- 메서드 호출
- 람다는 직접 호출이고 익명 클래스는 동적 디스패치 한다.
```java
//람다
//컴파일 시점에 어떤 메서드를 호출할지 결정됨 (정적 연결)
컴파일러: "c.accept를 호출하면"
컴파일러: "lambda$main$0 메서드를 실행하라"
컴파일러: "이 정보를 바이트코드에 기록"
c.accept("안녕") 호출
    ↓
(타입 확인 없이 바로)
    ↓
lambda$main$0("안녕") 호출 (이미 결정됨)
    ↓
System.out.println("안녕")
```
```java
//익명 클래스
//런타임에 객체 타입을 보고 메서드를 찾음 (동적 디스패치)
c.accept("안녕") 호출
    ↓
c 변수의 타입은 Consumer지만, 실제 객체는?
    ↓
힙에 있는 객체를 확인 → MyClass$1 타입
    ↓
MyClass$1의 vtable(가상 메서드 테이블)로 이동: accept → 0x1234 확인
    ↓
0x1234 주소의 메서드 실행 = MyClass$1.accept(String) 메서드
    ↓
System.out.println("안녕") 실행
```
- 정적으로 결정하면 타입 안정성, 성능에 좋다.
- 람다는 클래스를 런타임에 생성하고, 익명 클래스는 컴파일에 생성한다.
- 런타임에 .class 파일 아니고 메모리에만 생성하니까..
- 람다는 완전한 정적 시스템이라고 할 수 있다.
- 익명 클래스는 하이브리드 시스템이다.