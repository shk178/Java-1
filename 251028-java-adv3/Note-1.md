# 3. 람다
- 251028-java-adv3/src/lambda/InstanceMain.java
- 람다를 사용하면 익명 클래스 사용 때의 보일러플레이트 코드를 줄일 수 있다.
- 함수형 인터페이스일 때 람다가 익명 클래스를 대체할 수 있다.
- 람다도 익명 클래스처럼 인스턴스가 생성된다.
- 문법 설탕(코드를 간결하게 만드는 문법적 편의) - 람다가 익명 클래스의 문법 설탕처럼 사용될 수 있다.
## 함수형 인터페이스
- 추상 메서드 한 개만 가지는 인터페이스를 말한다.
- 단일 추상 메서드 = SAM (Single Abstract Method)
- 람다는 클래스, 추상 클래스에는 할당할 수 없다.
- 람다는 함수형 인터페이스에만 할당할 수 있다.
```java
public interface NotSamInterface {
    void run();
    void go();
}
// run, go 앞에 abstract가 생략돼 있다. (추상 메서드)
```
- @FunctionalInterface: 컴파일 시점에 단일 추상 메서드인지 체크한다.
```java
@FunctionalInterface
public interface SamInterface {
    void run();
}
```
- 251028-java-adv3/src/lambda2/Main3.java
- 람다를 대입하는 것은 람다 인스턴스의 참조값을 대입하는 것이다.
- 람다 인스턴스를 인자로 해서 메서드에 전달할 수도 있고, 메서드가 람다 인스턴스를 반환할 수도 있다.
## 고차 함수 (Higher-Order Function)
- 함수를 다루는 추상화 수준이 더 높다.
- 보통 함수는 데이터(값)을 입력 받고, 값을 반환한다.
- 고차 함수는 함수를 인자로 받거나 함수를 반환한다.
# 4. 함수형 인터페이스
```java
@FunctionalInterface
public interface ObjectFunction {
    Object apply(Object o);
}
```
- apply 구현 시 캐스팅, return 값 받을 때 캐스팅한다.
```java
@FunctionalInterface
public interface GenericFunction<T, R> {
    R apply(T t);
}
```
- <T, R>에 명시하니까 캐스팅 안 해도 된다.
- 람다의 대입을 생각해보면, 메서드 시그니처만 맞으면 된다.
- 그런데 같은 메서드라도, 인터페이스가 서로 다른 타입이라서
- InterfaceB = InterfaceA로는 람다 대입이 안 된다.
- 즉, 람다(익명 함수)는 그 자체로 타입이 정해지는 게 아니라
- 대입되는 함수형 인터페이스에 의해 타입이 정해진다. (타겟 타입)
- 정해진 이후에는 같은 타입에만 대입이 가능하다.
## 자바가 기본으로 제공하는 함수형 인터페이스
- 대입이 안 된다 = 호환성 문제다.
- 자바 제공 인터페이스를 사용하면 해결된다.
- C:\Users\user\.jdks\ms-21.0.8!\java.base\java\util\function
- 함수형 인터페이스 = 추상 메서드가 딱 1개인 인터페이스
- 추상 메서드 (1개, 핵심 기능) - ;로 끝남
- default 메서드 (여러 개 가능, 보조 기능) - {로 시작
- static 메서드 (여러 개 가능, 유틸리티 기능) - {로 시작
- Object 클래스의 메서드 오버라이드 (equals, hashCode 등)
### 1. 기본 함수형 인터페이스 (Generic)
#### Supplier<T>
```java
T get(); // 매개변수 없이 값을 반환 (공급)
```
- 예: `() -> new ArrayList<>()`
#### Consumer<T>
```java
void accept(T t); // 값을 받아서 소비 (반환값 없음)
Consumer<T> andThen(Consumer<? super T> after); // 순차적으로 두 Consumer 실행
```
- 예: `s -> System.out.println(s)`
#### Function<T, R>
```java
R apply(T t); // T를 받아서 R로 변환
<V> Function<V, R> compose(Function<? super V, ? extends T> before); // before 먼저 실행 후 현재 함수 실행
<V> Function<T, V> andThen(Function<? super R, ? extends V> after); // 현재 함수 실행 후 after 실행
static <T> Function<T, T> identity(); // 입력값을 그대로 반환하는 함수
```
- 예: `s -> s.length()`
#### Predicate<T>
```java
boolean test(T t); // 조건 검사 (true/false 반환)
Predicate<T> and(Predicate<? super T> other); // 논리 AND 연산
Predicate<T> negate(); // 논리 NOT 연산 (결과 반전)
Predicate<T> or(Predicate<? super T> other); // 논리 OR 연산
static <T> Predicate<T> isEqual(Object targetRef); // 대상과 동일한지 검사
static <T> Predicate<T> not(Predicate<? super T> target); // Predicate를 부정
```
- 예: `n -> n > 0`
#### UnaryOperator<T> (Function의 특수 케이스: T → T)
```java
static <T> UnaryOperator<T> identity(); // 입력값을 그대로 반환
```
- 예: `n -> n * 2`
### 2. Bi- 접두사 (두 개의 매개변수)
#### BiConsumer<T, U>
```java
void accept(T t, U u); // 두 값을 받아서 소비
<U> BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after); // 순차 실행
```
- 예: `(key, value) -> map.put(key, value)`
#### BiFunction<T, U, R>
```java
R apply(T t, U u); // 두 값을 받아서 R로 변환
<V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after); // 결과에 추가 함수 적용
```
- 예: `(a, b) -> a + b`
#### BiPredicate<T, U>
```java
boolean test(T t, U u); // 두 값으로 조건 검사
BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other); // AND 연산
BiPredicate<T, U> negate(); // NOT 연산
BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other); // OR 연산
```
- 예: `(name, age) -> age >= 18`
#### BinaryOperator<T> (BiFunction의 특수 케이스: T, T → T)
```java
static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator); // 두 값 중 작은 값 반환
static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator); // 두 값 중 큰 값 반환
```
- 예: `(a, b) -> a + b`
### 3. Int 특화 함수형 인터페이스
#### IntSupplier
```java
int getAsInt(); // int 값 공급
```
#### IntConsumer
```java
void accept(int value); // int 값 소비
IntConsumer andThen(IntConsumer after); // 순차 실행
```
#### IntFunction<R>
```java
R apply(int value); // int를 받아서 R로 변환
```
#### IntPredicate
```java
boolean test(int value); // int 조건 검사
IntPredicate and(IntPredicate other); // AND 연산
IntPredicate negate(); // NOT 연산
IntPredicate or(IntPredicate other); // OR 연산
```
#### IntUnaryOperator (int → int)
```java
int applyAsInt(int operand); // int를 받아서 int 반환
IntUnaryOperator compose(IntUnaryOperator before); // before 먼저 실행
IntUnaryOperator andThen(IntUnaryOperator after); // 순차 실행
static IntUnaryOperator identity(); // 입력값 그대로 반환
```
#### IntBinaryOperator (int, int → int)
```java
int applyAsInt(int left, int right); // 두 int를 받아서 int 반환
```
#### 변환 함수들
```java
// IntToDoubleFunction
double applyAsDouble(int value); // int → double
// IntToLongFunction
long applyAsLong(int value); // int → long
```
### 4. Long 특화 함수형 인터페이스
#### LongSupplier
```java
long getAsLong(); // long 값 공급
```
#### LongConsumer
```java
void accept(long value); // long 값 소비
LongConsumer andThen(LongConsumer after); // 순차 실행
```
#### LongFunction<R>
```java
R apply(long value); // long을 받아서 R로 변환
```
#### LongPredicate
```java
boolean test(long value); // long 조건 검사
LongPredicate and(LongPredicate other); // AND 연산
LongPredicate negate(); // NOT 연산
LongPredicate or(LongPredicate other); // OR 연산
```
#### LongUnaryOperator (long → long)
```java
long applyAsLong(long operand); // long을 받아서 long 반환
LongUnaryOperator compose(LongUnaryOperator before); // before 먼저 실행
LongUnaryOperator andThen(LongUnaryOperator after); // 순차 실행
static LongUnaryOperator identity(); // 입력값 그대로 반환
```
#### LongBinaryOperator (long, long → long)
```java
long applyAsLong(long left, long right); // 두 long을 받아서 long 반환
```
#### 변환 함수들
```java
// LongToDoubleFunction
double applyAsDouble(long value); // long → double
// LongToIntFunction
int applyAsInt(long value); // long → int
```
### 5. Double 특화 함수형 인터페이스
#### DoubleSupplier
```java
double getAsDouble(); // double 값 공급
```
#### DoubleConsumer
```java
void accept(double value); // double 값 소비
DoubleConsumer andThen(DoubleConsumer after); // 순차 실행
```
#### DoubleFunction<R>
```java
R apply(double value); // double을 받아서 R로 변환
```
#### DoublePredicate
```java
boolean test(double value); // double 조건 검사
DoublePredicate and(DoublePredicate other); // AND 연산
DoublePredicate negate(); // NOT 연산
DoublePredicate or(DoublePredicate other); // OR 연산
```
#### DoubleUnaryOperator (double → double)
```java
double applyAsDouble(double operand); // double을 받아서 double 반환
DoubleUnaryOperator compose(DoubleUnaryOperator before); // before 먼저 실행
DoubleUnaryOperator andThen(DoubleUnaryOperator after); // 순차 실행
static DoubleUnaryOperator identity(); // 입력값 그대로 반환
```
#### DoubleBinaryOperator (double, double → double)
```java
double applyAsDouble(double left, double right); // 두 double을 받아서 double 반환
```
#### 변환 함수들
```java
// DoubleToIntFunction
int applyAsInt(double value); // double → int
// DoubleToLongFunction
long applyAsLong(double value); // double → long
```
### 6. Boolean 특화
#### BooleanSupplier
```java
boolean getAsBoolean(); // boolean 값 공급
```
### 7. 객체와 원시타입 혼합
#### ObjIntConsumer<T>
```java
void accept(T t, int value); // 객체와 int를 받아서 소비
```
#### ObjLongConsumer<T>
```java
void accept(T t, long value); // 객체와 long을 받아서 소비
```
#### ObjDoubleConsumer<T>
```java
void accept(T t, double value); // 객체와 double을 받아서 소비
```
#### ToIntFunction<T>
```java
int applyAsInt(T value); // T를 받아서 int로 변환
```
#### ToLongFunction<T>
```java
long applyAsLong(T value); // T를 받아서 long으로 변환
```
#### ToDoubleFunction<T>
```java
double applyAsDouble(T value); // T를 받아서 double로 변환
```
#### ToIntBiFunction<T, U>
```java
int applyAsInt(T t, U u); // 두 값을 받아서 int로 변환
```
#### ToLongBiFunction<T, U>
```java
long applyAsLong(T t, U u); // 두 값을 받아서 long으로 변환
```
#### ToDoubleBiFunction<T, U>
```java
double applyAsDouble(T t, U u); // 두 값을 받아서 double로 변환
```
- Supplier: 매개변수 없이 값을 반환
- Consumer: 값을 받아서 소비 (반환값 없음)
- Function: 값을 받아서 다른 타입으로 변환
- Predicate: 조건 검사 (boolean 반환)
- Operator: 같은 타입의 연산 (UnaryOperator: 단항, BinaryOperator: 이항)
- Bi-: 두 개의 매개변수
- Int/Long/Double: 원시 타입 특화 (박싱/언박싱 비용 절감)
- To-: 다른 타입으로 변환
- 패키지는 다르지만 Runnable도 함수형 인터페이스다.