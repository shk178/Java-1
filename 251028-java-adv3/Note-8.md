## 즉시 평가와 지연 평가
- 즉시 평가: 값/객체를 바로 생성하거나 계산하는 것
- 지연 평가: 값이 실제로 사용될 때까지 계산을 미룸
```java
    public static void main(String[] args) {
        DebugLogger log = new DebugLogger();
        log.setDebug(true);
        log.debug(10 + 20); // 10 + 20 계산 후 debug 호출 - print 호출됨
        log.setDebug(false);
        log.debug(10 + 20); // 10 + 20 계산 후 debug 호출 - print 호출x
        // 10 + 20은 항상 실행된다.
        // 10 + 20이 실행 안 되도록 연산 정의 시점과 실행 시점을 분리한다.
    }
```
- 익명 클래스를 만들고, 메서드를 나중에 호출
- 람다를 만들고, 람다를 나중에 호출
```java
public class LambdaDebug {
    private boolean isDebug = false;
    public boolean isDebug() {
        return isDebug;
    }
    public void setDebug(boolean flag) {
        isDebug = flag;
    }
    public void debug(Object msg) {
        if (isDebug) {
            print(msg);
        }
    }
    public void print(Object msg) {
        System.out.println("[DEBUG] " + msg);
    }
    public void debug(Supplier<?> supplier) {
        if (isDebug) {
            print(supplier.get());
        }
    }
}
public class Evaluation2 {
    public static void main(String[] args) {
        LambdaDebug log = new LambdaDebug();
        log.setDebug(true);
        log.debug(() -> one());
        //one 호출
        //[DEBUG] 1
        log.setDebug(false);
        log.debug(() -> one()); // one() 실행 안 됨
    }
    static int one() {
        System.out.println("one 호출");
        return 1;
    }
}
```
- orElse(T other): other을 항상 미리 계산한다. (즉시 평가)
- orElseGet(Supplier supplier): supplier 지연 평가
```java
public class Evaluation3 {
    public static void main(String[] args) {
        Optional<Integer> n = Optional.of(100);
        Optional<Integer> nn = Optional.empty();
        System.out.println("n.orElse(two())=" + n.orElse(two()));
        //two 실행
        //n.orElse(two())=100
        System.out.println("nn.orElse(two())=" + nn.orElse(two()));
        //two 실행
        //nn.orElse(two())=2
        System.out.println("n.orElseGet(() -> two())=" + n.orElseGet(() -> two()));
        //n.orElseGet(() -> two())=100
        System.out.println("nn.orElseGet(() -> two())=" + nn.orElseGet(() -> two()));
        //two 실행
        //nn.orElseGet(() -> two())=2
        Supplier<Integer> supplier = () -> two();
        System.out.println("n.orElse(supplier.get())=" + n.orElse(supplier.get()));
        //two 실행
        //n.orElse(supplier.get())=100
        System.out.println("nn.orElse(supplier.get())=" + nn.orElse(supplier.get()));
        //two 실행
        //nn.orElse(supplier.get())=2
    }
    static int two() {
        System.out.println("two 실행");
        return 2;
    }
}
```
- orElseGet이 지연 평가되는 건 Supplier가 함수형 인터페이스여서 그렇다.
- 람다는 코드 조각을 객체로 전달하는 것
```java
// 1. 일반 메서드 호출 - 즉시 평가
String result = createDefaultUser(); // 지금 바로 실행
userOpt.orElse(result); // 이미 실행된 결과를 전달
// 2. 람다 - 지연 평가
Supplier<String> supplier = () -> createDefaultUser(); // 실행 안 함, 코드만 담음
userOpt.orElseGet(supplier); // 필요할 때 supplier.get() 호출
@FunctionalInterface
public interface Supplier<T> {
    T get(); // 이 메서드를 호출할 때까지 실행 안 됨
}
```
- 스트림도 지연 평가를 하지만, 원리가 다르다.
- 스트림은 파이프라인 최적화를 위한 지연 평가, 람다는 필요할 때만 실행 위한 지연 평가
# 12. 디폴트 메서드
- 기존 인터페이스에 메서드를 추가하면, 기존 구현 클래스들에 컴파일 에러가 일어났다.
- 하휘 호환성 문제라고 한다.
- 디폴트 메서드는 인터페이스에서 메서드 본문(구현부)을 가지도록 해서 해당 문제를 해결했다.
- List 인터페이스에 sort(...) 디폴트 메서드 추가
- Collection 인터페이스에 stream() 디폴트 메서드 추가
- Iterable 인터페이스에 forEach 디폴트 메서드 추가
## 디폴트 메서드 사용법
- 하휘 호환성을 위해 최소한으로 사용
- 인터페이스는 여전히 추상화의 역할: 계약의 역할에 충실한 것이 좋다.
- 다중 상속(충돌) 문제
    - 하나의 클래스가 여러 인터페이스를 동시에 구현할 때
    - 서로 다른 인터페이스에 동일한 시그니처의 디폴트 메서드가 존재하면 충돌이 일어난다.
    - 이 경우 구현 클래스에서 메서드를 재정의해야 한다.
```java
interface A {
    default void hello() {
        System.out.println("A");
    }
}
interface B {
    default void hello() {
        System.out.println("B");
    }
}
public class C implements A, B {
    @Override
    public void hello() {
        //1. 직접 구현
        //2. A.super.hello();
        //3. B.super.hello();
    }
}
```
- 디폴트 메서드에 상태를 두지 않는다.
    - 인터페이스는 일반적으로 상태 없이 동작만 정의하는 추상화 계층
    - 디폴트 메서드도 구현을 일부 제공할 뿐, 호출 시 상태에 따라 동작이 달라지는 로직은 적절x
    - 상태 관련 로직은 추상 클래스 등으로 만든다.