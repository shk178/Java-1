# 6. 람다 대 익명 클래스
## 1. 공통점
- 인터페이스를 즉석에서 구현할 때 사용 가능 (특히 메서드가 하나만 있는 경우)
- 클래스 이름 없이 바로 객체 생성 가능
- 간결한 코드 작성에 도움
```java
button.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Clicked!");
    }
});
//또는
button.addActionListener(e -> System.out.println("Clicked!"));
```
## 2. 문법 차이

| 항목 | 익명 클래스 | 람다식 |
| -- | --- | --- |
| 형태 | new 인터페이스() { ... } | (매개변수) -> { ... } |
| 길이 | 장황함 | 매우 간결 |
| 타입 필요 여부 | 명시적 타입 필요 | 컴파일러가 추론 |
| 내부 this | 익명 클래스 자신 | 외부 클래스를 가리킴 |
| 생성 시점 | 새로운 클래스 생성 | 기존 인터페이스 구현체를 간결히 표현 |

## 3. 예제 비교
### 익명 클래스
```java
// 익명 클래스
Runnable runnable = new Runnable() {
    @Override
    public void run() {
        System.out.println("익명 클래스 실행");
    }
};
// 람다식
Runnable runnable = () -> System.out.println("람다 실행");
```
## 4. 내부 동작 차이 (컴파일 관점)

| 항목 | 익명 클래스 | 람다식 |
| -- | ---- |-----|
| 바이트코드 | 새로운 .class 파일 생성 (예: Outer$1.class) | JVM이 invokedynamic 명령어로 처리 (클래스 파일 내 동적으로 생성됨) |
| 메모리 사용 | 더 많음 | 더 효율적 |
| 생성 방식 | 런타임 시 새 클래스 로드 | 런타임 시 함수형 인터페이스 인스턴스로 처리 |

- 즉, 익명 클래스는 실제 클래스를 하나 더 만드는 것,
- 람다는 내부적으로 훨씬 가볍게 처리된다.
## 5. this / 변수 캡처 차이
```java
public class Demo {
    private String name = "Outer";
    void test() {
        Runnable r1 = new Runnable() {
            String name = "Inner";
            @Override
            public void run() {
                System.out.println(this.name); // Inner (익명 클래스 자신)
            }
        };
        Runnable r2 = () -> {
            System.out.println(this.name); // Outer (외부 클래스)
        };
        r1.run();
        r2.run();
    }
}
```
- 즉, this의 의미가 다르다
- 익명 클래스: 내부의 this
- 람다식: 외부 클래스의 this
## 6. 함수형 인터페이스만 사용 가능 (람다의 제약)
- 람다는 반드시 하나의 추상 메서드만 가진 인터페이스 (함수형 인터페이스)에만 사용 가능
```java
@FunctionalInterface
interface MyFunc {
    void doSomething();
}
```
- 익명 클래스는 추상 메서드 여러 개인 인터페이스나 추상 클래스도 구현할 수 있지만
- 람다는 단 하나의 추상 메서드만 있는 인터페이스에만 가능
## 람다식과 익명 클래스의 상속 관점 차이
- 이 부분이 람다식은 익명 클래스의 단축 문법이 아니라는 걸 보여준다.

| 구분 | 익명 클래스 | 람다식 |
| -- | -- |-----|
| 상속 가능성 | 가능 | 불가능 |
| 구현 대상 | 인터페이스 or 추상 클래스 | 오직 함수형 인터페이스 (추상 메서드 1개) |
| 클래스 생성 여부 | 새 클래스 생성됨 (Outer$1.class) | 클래스 생성 없음 (메서드 참조) |

- 즉, 익명 클래스는 상속 구조 안에 들어갈 수 있다.
- 람다식은 함수만 표현할 수 있다.
- 익명 클래스는 클래스이기 때문에 다른 클래스를 상속하거나 인터페이스를 여러 개 구현할 수 있다.
### 예시: 추상 클래스 상속
```java
abstract class Animal {
    abstract void sound();
}
public class Main {
    public static void main(String[] args) {
        Animal dog = new Animal() { // 추상 클래스 상속
            @Override
            void sound() {
                System.out.println("멍멍!");
            }
        };
        dog.sound();
    }
}
// 컴파일 에러
Animal dog = () -> System.out.println("멍멍!");
```
- 익명 클래스가 내부적으로 Animal을 상속받은 Outer$1 클래스를 만들어서 인스턴스를 생성
- 람다는 함수형 인터페이스의 인스턴스일 뿐, 클래스를 상속하거나 메서드를 오버라이딩할 수 없다.

| 기능 | 익명 클래스 | 람다식 |
| -- |----| --- |
| 추상 클래스 상속 | 가능 | 불가능 |
| 인터페이스 여러 개 구현 | 가능 (컴파일러가 클래스 생성) | 하나의 함수형 인터페이스만 |
| 메서드 오버라이드 여러 개 | 가능 | 단 하나만 가능 |
| 메서드나 필드 추가 | 가능 (클래스 몸체에 작성 가능) | 불가능 (함수 표현식이므로 몸체 없음) |

### 예시: 메서드 추가
```java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("실행!");
    }

    void extra() { // 추가 메서드
        System.out.println("추가 메서드!");
    }
};
```
- 익명 클래스는 extra() 같은 자기만의 메서드를 가질 수 있음
- 람다는 그런 걸 정의할 수 없음 — 단지 하나의 함수만 표현

| 항목 | 익명 클래스 | 람다식 |
| -- | ------ | --- |
| 생성 방식 | 새로운 클래스를 상속해서 정의 | invokedynamic 호출로 함수 객체 생성 |
| 상속 관계 | 명확한 상속 트리 존재 | 없음 (인터페이스 구현체로만 표현됨) |
| 클래스 파일 | 새로 생김 (Outer$1.class) | 생기지 않음 |

- 익명 클래스는 새로운 클래스 타입을 정의하는 문법
- 람다는 기존 인터페이스 타입을 구현하는 함수 표현식
## 람다와 객체, 그리고 메서드 참조의 내부 동작

| 개념 | 설명 |
| -- | -- |
| 함수형 인터페이스 | 추상 메서드가 1개인 인터페이스. 예: Runnable, Predicate<T> |
| 람다식 | 함수형 인터페이스의 인스턴스를 표현식으로 생성하는 문법 |
| 메서드 참조 | 이미 존재하는 메서드를 람다 대신 참조하여 전달하는 문법 (ClassName::methodName) |
| Method 클래스 (리플렉션) | 실제 메서드 정의(메타데이터)를 표현하는 자바 리플렉션 객체 |

- 람다식은 객체인가 - 그렇다.
- 람다식은 함수형 인터페이스의 인스턴스(객체)로 컴파일된다.
```java
Runnable r = () -> System.out.println("Hello");
// 컴파일러가 내부적으로 다음과 비슷하게 처리
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};
// 하지만 실제로는 이렇게 익명 클래스로 바꾸는 게 아니라
// invokedynamic 명령어로 함수형 인터페이스 구현 객체를 동적으로 생성
// 즉, 클래스 파일(Outer$1.class)이 생기지 않는다.
```
- 람다식이 생성될 때, JVM은 익명 클래스 객체처럼 보이는 함수형 인터페이스 인스턴스를
- LambdaMetafactory.metafactory() 라는 내부 API를 통해 만들어낸다.
- 즉, 람다는 자신만의 클래스가 없음
- 대신 JVM이 동적으로 생성한 익명 구현체 인스턴스를 만듦 (메모리상에서, 클래스 파일은 없음)
```java
Runnable r = () -> System.out.println("Hello");
System.out.println(r.getClass()); // class Main$$Lambda$1/0x0000000800b00440
```
- 즉, Main$$Lambda$1 같은 JVM이 런타임에 생성한 익명 클래스
- 이 클래스는 Runnable을 구현하고, run()을 오버라이드
### 람다식이 메서드 참조로 생성되면
```java
class Printer {
    void print(String s) {
        System.out.println(s);
    }
}
Printer p = new Printer();
Runnable r = p::toString;
```
- p::toString은 p 객체의 toString() 메서드 호출을 지연 실행하는 람다 표현
- 컴파일러는 이것을 다음과 같이 해석
- 이 Runnable의 run()이 호출될 때, p.toString()을 실행하는 함수형 객체를 만들어라
- 그래서 이 경우에도 새로운 익명 클래스는 생성되지 않는다.
- 대신 p 객체에 대한 메서드 핸들(MethodHandle)이 만들어지고, 그걸로부터 람다 인스턴스가 생성

| 항목 | 설명 |
| -- | -- |
| p::toString | p 객체의 toString()을 호출하는 함수형 인터페이스 구현체” |
| 생성 위치 | Printer 클래스가 아니라 호출한 클래스(예: Main) |
| 내부 구조 | Printer의 메서드를 가리키는 MethodHandle을 캡처함 |
| 생성 클래스 | Main$$Lambda$... 형태의 람다 구현체 클래스 (JVM이 동적으로 생성) |

- 즉, 람다식이 정의된 클래스의 객체로 생성되는 게 아니라,  JVM이 만들어낸 별도의 람다 구현체 인스턴스로 생성
- 함수형 인터페이스의 메서드는 Method 클래스로 만들어지는가 - 아니다.
- 리플렉션 API의 Method 객체는 메서드의 메타데이터를 나타낼 뿐, 실행 가능한 코드 객체가 아니다.
- Method 객체는 메서드의 정보(이름, 리턴타입, 매개변수)를 표현
- 람다 인스턴스의 실행 코드는 MethodHandle로 참조
```
람다식: (x) -> System.out.println(x)
    ↓ 컴파일러 변환
invokedynamic #... LambdaMetafactory.metafactory(...)
    ↓ 런타임
JVM이 Main$$Lambda$1 인스턴스 생성
    ↓
Main$$Lambda$1 implements Consumer<String>
  → accept(x) { System.out.println(x); }
```
- System.out::println 같은 메서드 참조 시
```
invokedynamic #... LambdaMetafactory.metafactory(
    targetMethod=MethodHandle(System.out.println),
    functionalInterfaceMethod=Consumer.accept
)
```
- 람다는 코드를 데이터처럼 다루는 문법적 표현이며
- 실제로는 JVM이 런타임에 함수형 인터페이스 구현 객체를 동적으로 생성하고
- 그 내부에서 메서드 참조는 MethodHandle로 연결된다.
### 익명 클래스, 람다 - 캡처링 적용된다.
- effectively final일 때 지역 변수 캡처할 수 있다.