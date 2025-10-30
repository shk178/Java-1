# 7. 메서드 참조
- 메서드 참조는 람다 표현식을 더 간결하게 쓸 수 있게 해주는 문법
- 람다식에서 이미 존재하는 메서드를 단순히 호출만 하는 경우에 쓴다.
```
(매개변수) -> 객체.메서드(매개변수) // 객체::메서드 형태로 줄인다.
```
- 정적 메서드 참조: `클래스명::메서드명`
```java
Function<String, Integer> f1 = (s) -> Integer.parseInt(s);
Function<String, Integer> f2 = Integer::parseInt; // 메서드 참조
System.out.println(f2.apply("123")); // 123
// 람다 (s) -> Integer.parseInt(s)는 입력값 s를 그대로 parseInt에 전달하므로
// 바로 Integer::parseInt로 바꿀 수 있다.
```
- 특정 객체의 인스턴스 메서드 참조: `객체명::메서드명`
```java
String greeting = "Hello";
Supplier<String> s1 = () -> greeting.toLowerCase();
Supplier<String> s2 = greeting::toLowerCase; // 메서드 참조
System.out.println(s2.get()); // hello
// greeting이라는 특정 객체의 메서드를 가리키고 있다.
```
- 임의 객체의 인스턴스 메서드 참조: `클래스명::메서드명`
```java
Function<String, Integer> f1 = (str) -> str.length();
Function<String, Integer> f2 = String::length; // 메서드 참조
System.out.println(f2.apply("Java")); // 4
// (String s) -> s.length()와 같은 패턴은
// String 클래스의 임의 객체 s의 메서드 호출을 의미하므로 String::length로 쓸 수 있다.
List<String> names = List.of("Tom", "Jerry", "Spike");
names.forEach(System.out::println); // 각 문자열의 toString이 자동 호출됨
```
- 생성자 참조: `클래스명::new`
```java
Supplier<List<String>> s1 = () -> new ArrayList<>();
Supplier<List<String>> s2 = ArrayList::new; // 생성자 참조
List<String> list = s2.get();
// 생성자를 참조하여 새 객체를 생성하는 메서드로 사용할 수 있다.
Function<String, Person> f1 = (name) -> new Person(name);
Function<String, Person> f2 = Person::new; // 생성자 참조
```
- 메서드 참조는 스트림 API에서 자주 사용된다.
```java
List<String> names = List.of("Alice", "Bob", "Charlie");
// 람다식
names.stream()
        .map(name -> name.toUpperCase())
        .forEach(name -> System.out.println(name));
// 메서드 참조
names.stream()
        .map(String::toUpperCase)
        .forEach(System.out::println);
```
- 251028-java-adv3/src/lambda5
- 메서드 참조를 작성할 때 매개변수는 생략한다.
- 메서드 참조는 람다식의 축약형이다.
- 컴파일러가 함수형 인터페이스의 시그니처를 보고 어떤 메서드를 참조할지 추론한다.
# 7. 스트림 API
- Java Stream API는 Java 8부터 도입된 기능
- 컬렉션, 배열, I/O 채널 등의 데이터를 선언적이고 함수형 스타일로 처리할 수 있게 해주는 도구
- 즉, for문이나 while문 등을 직접 쓰지 않고도 데이터를 필터링, 변환, 집계, 정렬 등을 수행할 수 있다.
- 스트림(Stream)이란 데이터의 흐름(Flow)을 추상화한 객체
- 데이터를 하나씩 흘려보내면서 중간에 가공하고, 마지막에 결과를 모은다.
## 스트림의 주요 특징
- 데이터를 저장하지 않음: 스트림은 데이터 소스를 변경하지 않고, 단지 데이터를 흘려보내며 처리
- 1회성 사용: 한 번 사용한 스트림은 재사용할 수 없다. (다시 stream()을 만들어야 함)
- 지연(lazy) 연산: 최종 연산이 호출될 때까지 실제 계산을 하지 않는다.
- 함수형 스타일: 람다 표현식을 활용하여 코드가 간결
- 병렬 처리 가능: parallelStream()을 사용하면 멀티코어 CPU에서 자동 병렬 처리
## 스트림의 구성 단계
- 1. 생성 (Creation)
- 데이터 소스로부터 스트림을 생성
- 예: list.stream(), Stream.of(1,2,3), Arrays.stream(array)
- 2. 중간 연산 (Intermediate Operations)
- 데이터를 가공 (필터링, 변환 등)
- 예: filter(), map(), sorted(), distinct(), limit()
- 3. 최종 연산 (Terminal Operations)
- 결과를 산출 (출력, 합계, 수집 등)
- 예: forEach(), count(), collect(), sum(), reduce()
## 자주 쓰이는 연산 예시

| 연산                     | 설명               | 예시                                  |
| ---------------------- | ---------------- | ----------------------------------- |
| `filter(Predicate)`    | 조건에 맞는 요소만 남김    | `filter(n -> n > 10)`               |
| `map(Function)`        | 각 요소를 변환         | `map(String::toUpperCase)`          |
| `sorted()`             | 정렬               | `sorted(Comparator.reverseOrder())` |
| `distinct()`           | 중복 제거            | `distinct()`                        |
| `limit(n)` / `skip(n)` | 일부 요소만 선택        | `limit(5)`                          |
| `forEach()`            | 각 요소에 대해 동작 수행   | `forEach(System.out::println)`      |
| `collect()`            | 결과를 컬렉션 등으로 변환   | `collect(Collectors.toList())`      |
| `reduce()`             | 누적 계산 (합계, 평균 등) | `reduce(0, Integer::sum)`           |

- 예시: 객체 리스트에서 특정 필드 추출
```java
class Person {
    String name;
    int age;
    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
List<Person> people = List.of(
    new Person("Alice", 23),
    new Person("Bob", 30),
    new Person("Charlie", 27)
);
List<String> names = people.stream()
        .filter(p -> p.age >= 25)
        .map(p -> p.name)
        .collect(Collectors.toList());
System.out.println(names); // [Bob, Charlie]
```
- 예시: 병렬 스트림
```java
List<Integer> numbers = IntStream.rangeClosed(1, 1_000_000)
        .boxed()
        .collect(Collectors.toList());
long sum = numbers.parallelStream()
        .mapToLong(Integer::longValue)
        .sum();
// parallelStream()을 쓰면 내부적으로 여러 스레드에서 데이터를 병렬로 처리 (병렬 오버헤드 주의)
```
## Stream Pipeline (스트림 파이프라인)
```
[ 데이터 소스 ] → [ 중간 연산 ] → [ 최종 연산 ]
```
- 데이터 소스 단계: 스트림이 흘러나오는 원천 - `List`, `Set`, `Array`, `File`, `IntStream.range()` 등
- 중간 연산 단계: 데이터를 변환하거나 필터링 (결과: 새 스트림 반환) - `filter()`, `map()`, `sorted()` 등
- 최종 연산 단계: 파이프라인을 종료하고 실제 결과를 만듦 - `collect()`, `forEach()`, `sum()`, `count()` 등
- 스트림은 최종 연산이 호출될 때까지 아무 일도 하지 않는다.
## Lazy Evaluation (지연 연산)
- 스트림의 중간 연산은 게으르게(lazily) 실행된다.
- 즉, 최종 연산이 호출되기 전에는 실제 데이터 처리를 하지 않는다.
- 최종 연산이 호출되어야만 스트림이 실제로 데이터에 접근하고 계산을 수행한다.
- 성능 최적화 때문: 필요 없는 연산을 미리 수행하지 않고, 한 요소씩 처리하면서 조건에 따라 바로 종료할 수 있다.
- 예: findFirst(), limit() 등의 조기 종료 short-circuit 연산
```java
List<String> names = List.of("Alice", "Bob", "Charlie", "David");
Stream<String> stream = names.stream()
    .filter(name -> {
        System.out.println("Filtering: " + name);
        return name.length() > 3;
    })
    .map(name -> {
        System.out.println("Mapping: " + name);
        return name.toUpperCase();
    });
// 아직 아무 것도 출력되지 않음
stream.collect(Collectors.toList()); 
// 이 시점에서 전체 연산 실행
/*
Filtering: Alice
Mapping: Alice
Filtering: Bob
Filtering: Charlie
Mapping: Charlie
Filtering: David
Mapping: David
 */
```
## Internal Iteration (내부 반복)
- 전통적인 컬렉션 처리에서는 직접 루프를 돌린다.
- 외부 반복이라고 부른다. — 반복 제어를 직접 하는 방식
- 스트림은 내부 반복이라고 부른다.
- 스트림 내부에서 자동으로 최적화된 방식으로 반복 로직을 수행
```java
// 전통 방식
for (String name : names) {
    if (name.length() > 3) {
        System.out.println(name.toUpperCase());
    }
}
// 스트림
names.stream()
        .filter(n -> n.length() > 3)
        .map(String::toUpperCase)
        .forEach(System.out::println);
```
- 내부 반복의 장점
  - 반복 제어를 라이브러리가 관리해 코드가 간결
  - 최적화(병렬 처리, 파이프라인 최적화)가 가능
- 내부적으로 실제로 어떻게 동작하나
  - stream()을 호출하면 데이터 소스(Collection 등)에서 Stream 객체를 생성
  - 이 객체는 데이터를 담지 않고, 연산 단계 목록만 저장
  - .filter() 등의 중간 연산은 새로운 Stream 객체를 반환하면서 연산 단계를 체인 연결
  - .collect() 또는 .forEach() 같은 최종 연산을 만나면
  - 스트림 파이프라인이 평가되며 소스 데이터를 하나씩 꺼내 각 단계의 람다를 순서대로 적용
- 예: 실제 파이프라인 처리 순서
```java
List<String> list = List.of("a", "bb", "ccc");
list.stream()
    .filter(s -> {
        System.out.println("filter: " + s);
        return s.length() > 1;
    })
    .map(s -> {
        System.out.println("map: " + s);
        return s.toUpperCase();
    })
    .forEach(System.out::println);
/*
filter: a
filter: bb
map: bb
BB
filter: ccc
map: ccc
CCC
 */
```
- 각 요소가 모든 중간 연산 단계를 한 번에 거쳐서 최종 연산에 도달
- 즉, 요소별로 순차 처리된다.
- 연산별로 요소들이 한 번에 처리되는 게 아니다.
- 병렬 스트림 내부 구조
```
parallelStream()을 사용하면 내부적으로 ForkJoinPool을 사용하여 데이터를 병렬로 분할해 처리합니다.
데이터를 여러 “서브 스트림”으로 나눔 (split)
각 스레드가 자신에게 할당된 데이터를 처리
결과를 합침 (reduce)
내부에서는 Spliterator라는 인터페이스가 분할(splitting)과 순회(iteration)를 담당
```
## 스트림 파이프라인 - 평가(evaluation)
```java
// 스트림은 filter(), map() 같은 중간 연산(intermediate operation)과
// collect(), forEach(), count() 같은 최종 연산(terminal operation)으로 구성
List<String> result = list.stream()
                .filter(s -> s.length() > 3)
                .map(String::toUpperCase)
                .collect(Collectors.toList());
// 이 코드는 겉보기엔 실행되는 것 같지만, 평가되지 않았다. (not evaluated yet)
// 즉, .filter()와 .map()은 무엇을 해야 할지를 정의할 뿐, 아직 데이터에 접근하거나 계산을 수행하지 않는다.
/*
이 상태의 스트림은 실제 데이터를 들고 있지 않다.
내부적으로는 이렇게 구성되어 있다:
Stream(source)
 ├─ filter()  → Predicate 저장
 ├─ map()     → Function 저장
 └─ collect() → Collector 실행 요청
 */
// 최종 연산(collect())이 호출되는 순간, 스트림은 이제 데이터를 흘려보내야겠구나 하고
// 각 단계의 람다를 실제로 적용하면서 데이터 소스를 순회하기 시작
// 이걸 파이프라인이 평가(evaluated)된다고 한다.
```
- 평가된다 = 실제 데이터가 소스에서 읽혀서, 각 중간 연산이 실행되며, 최종 결과가 만들어진다.
## 스트림 파이프라인 - 단락 연산, 단락 평가(short-circuit)
- short-circuit는 스트림 파이프라인이 필요 이상으로 모든 요소를 처리하지 않도록 하는 최적화
- 즉, 결과가 확정되면 즉시 평가를 멈춘다.

| 메서드           | 설명                     |
| ------------- | ---------------------- |
| `findFirst()` | 첫 번째 요소를 찾으면 즉시 종료     |
| `findAny()`   | 병렬일 때 아무 요소나 찾으면 종료    |
| `anyMatch()`  | 조건을 만족하는 요소를 찾으면 종료    |
| `allMatch()`  | 조건을 만족하지 않는 요소를 찾으면 종료 |
| `noneMatch()` | 조건을 만족하는 요소가 발견되면 종료   |
| `limit(n)`    | n개 요소 처리 후 종료          |

```java
List<String> list = List.of("apple", "banana", "cherry", "date");
boolean hasShort = list.stream()
        .peek(s -> System.out.println("Checking: " + s))
        .anyMatch(s -> s.startsWith("c"));
/*
Checking: apple
Checking: banana
Checking: cherry
 */
// "cherry"에서 조건이 참이 되자마자 스트림이 즉시 종료
// "date"는 전혀 평가되지 않는다.
// 이렇게 불필요한 요소를 평가하지 않는 걸 short-circuit optimization이라고 부른다.
```
## 스트림 파이프라인 - 연산 융합(fusion)
- 스트림의 중간 연산들을 하나의 루프로 합쳐서 실행하는 최적화 기법
- 스트림의 lazy evaluation 덕분에 가능한 일
```java
List<String> result = list.stream()
        .filter(s -> s.length() > 3)
        .map(String::toUpperCase)
        .collect(Collectors.toList());
// 겉보기엔 filter()가 전체 데이터를 한 번 훑고,
// 그다음 map()이 다시 한 번 전체 데이터를 도는 것처럼 보인다.
// 즉, 2-pass (2번 순회)로 보인다.
// 하지만 실제로는 그렇지 않다.
/* 스트림은 각 요소가 흘러가면서 모든 중간 연산을 한 번에 수행 */
for (String s : list) {
    if (s.length() > 3) {               // filter
        String upper = s.toUpperCase(); // map
        result.add(upper);              // collect
    }
}
// 실제로는 이렇게 실행된다.
// 하나의 루프 안에서 filter와 map이 융합(fused)된 형태로 처리된다.
// 중간 리스트를 만들지 않고, CPU 캐시 효율도 높고, 메모리 사용량이 줄어든다.
```
- Short-circuit + Fusion 결합
```java
List<String> list = List.of("a", "bb", "ccc", "dddd");
Optional<String> result = list.stream()
        .filter(s -> s.length() > 2)
        .map(String::toUpperCase)
        .findFirst();
// 1. "a" → filter(false) → 버림
// 2. "bb" → filter(false) → 버림
// 3. "ccc" → filter(true) → map("CCC") → findFirst() 만족 → 즉시 종료
/*
Stream Pipeline 정의 (filter, map, ...)
    ↓  (lazy)
최종 연산 호출 (collect, findFirst 등)
    ↓
파이프라인 평가 시작 (evaluation)
    ↓
각 요소에 대해 중간 연산을 융합(fusion) 처리
    ↓
단락 조건 발생 시 short-circuit으로 조기 종료
    ↓
결과 반환 및 스트림 종료
 */
```
## 자주 쓰이는 연산들 내부 구현
```
Stream (추상 클래스)
 ├─ ReferencePipeline (구현 클래스)
 │   ├─ Head (소스 단계)
 │   ├─ StatelessOp (filter, map 등)
 │   ├─ StatefulOp (sorted, distinct 등)
 │   └─ TerminalOp (collect, forEach 등)
```
- 각 중간 연산 - ReferencePipeline.StatelessOp 또는 StatefulOp의 하위 클래스로 구현되어 있다.
- 각 연산은 내부적으로 Sink를 사용하여 데이터 흐름을 체인 형태로 연결
### Sink 체인(Sink Chain)
- 모든 스트림 연산은 내부적으로 Sink 객체를 통해 연결된다.
- `Source → filter → map → collect`
```
SinkHead (데이터 소스)
    ↓
SinkFilter (filter 연산)
    ↓
SinkMap (map 연산)
    ↓
SinkTerminal (collect 연산)
```
```java
interface Sink<T> {
    void begin(long size); // 시작 전 초기화
    void accept(T value); // 각 요소를 처리
    void end(); // 종료 시 정리
}
// 데이터는 accept()를 통해 다음 Sink로 전달
// filter가 accept()하면 map이 그 값을 받아서 또 accept() 하는 식
```
- filter()
```java
// 호출 코드
Stream<T> filtered = stream.filter(predicate);
// 내부 구현
@Override
public final Stream<P_OUT> filter(Predicate<? super P_OUT> predicate) {
    // null 체크: predicate가 null이면 NullPointerException 발생
    Objects.requireNonNull(predicate);

    // filter()는 중간 연산(intermediate operation)으로,
    // 새로운 StatelessOp(상태 없는 연산자)를 생성하여 Stream 파이프라인에 추가함
    // StreamShape.REFERENCE는 참조 타입(예: 객체)을 처리한다는 의미
    // StreamOpFlag.NOT_SIZED는 필터링 결과의 크기를 미리 알 수 없다는 플래그
    return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE,
            StreamOpFlag.NOT_SIZED) {

        // opWrapSink는 실제 데이터를 처리하는 Sink를 감싸는 메서드
        // Sink는 Stream의 각 요소를 소비하는 소비자 역할을 함
        @Override
        Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> downstream) {
            // ChainedReference는 참조 타입을 처리하는 Sink의 구현체
            // downstream은 다음 연산자에게 데이터를 전달하는 Sink
            // 여기서는 predicate 조건을 만족하는 요소만 downstream으로 전달함
            return new Sink.ChainedReference<P_OUT, P_OUT>(downstream) {
                @Override
                public void accept(P_OUT u) {
                    // predicate.test(u): 조건 검사
                    // 조건을 만족하면 downstream으로 전달
                    if (predicate.test(u)) {
                        downstream.accept(u);
                    }
                    // 조건을 만족하지 않으면 해당 요소는 필터링되어 버려짐
                }
            };
        }
    };
}
// Stream은 lazy evaluation(지연 평가)을 사용함. 즉, 최종 연산이 실행되기 전까지 중간 연산은 수행되지 않음.
// filter는 중간 연산이므로 데이터를 즉시 처리하지 않고 파이프라인에 연산을 추가만 함.
// upstream은 현재 연산 이전의 연산자들을 의미하며, filter에서 요소를 버려도 upstream은 계속 동작함.
// fusion은 여러 중간 연산을 하나의 연산으로 합쳐서 성능을 최적화하는 기법임.
```
- map()
```java
Stream<R> mapped = stream.map(mapper);
@Override
public final <R> Stream<R> map(Function<? super P_OUT, ? extends R> mapper) {
    Objects.requireNonNull(mapper);
    return new StatelessOp<P_OUT, R>(this, StreamShape.REFERENCE,
            StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT) {
        @Override
        Sink<P_OUT> opWrapSink(int flags, Sink<R> downstream) {
            return new Sink.ChainedReference<P_OUT, R>(downstream) {
                @Override
                public void accept(P_OUT u) {
                    downstream.accept(mapper.apply(u));
                }
            };
        }
    };
}
// map()도 StatelessOp으로 구현
// accept()가 호출되면, mapper 함수를 적용하고 그 결과를 downstream으로 전달
// 즉, map은 데이터 변환만 담당하며 상태를 저장하지 않는다.
```
- sorted()
```java
Stream<T> sorted = stream.sorted();
@Override
public final Stream<P_OUT> sorted() {
    return new StatefulOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE,
            StreamOpFlag.IS_ORDERED | StreamOpFlag.NOT_SIZED) {
        @Override
        <P_IN> Node<P_OUT> opEvaluateParallel(PipelineHelper<P_OUT> helper,
                                              Spliterator<P_IN> spliterator,
                                              IntFunction<P_OUT[]> generator) {
            // 병렬 스트림의 경우 전체 수집 후 정렬 수행
            return collectAndSort(helper, spliterator, comparator);
        }

        @Override
        Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> downstream) {
            // 순차 스트림의 경우에도 버퍼에 저장 후 한 번에 정렬
            return new Sink.ChainedReference<P_OUT, P_OUT>(downstream) {
                List<P_OUT> buffer;
                @Override
                public void begin(long size) { buffer = new ArrayList<>(); }
                @Override
                public void accept(P_OUT t) { buffer.add(t); }
                @Override
                public void end() {
                    buffer.sort(comparator);
                    buffer.forEach(downstream::accept);
                }
            };
        }
    };
}
// sorted()는 전체 요소를 버퍼에 저장한 뒤 정렬 후 전달
// 즉, lazy하지만 Stateful(상태를 가진) 연산이라
// 모든 데이터를 모은 다음에야 다음 단계로 넘긴다.
// distinct(), limit() 같은 연산도 유사한 구조를 가진다.
// sorted()는 fusion이 불가능 (데이터 전체가 필요하기 때문)
```
- collect()
```java
List<T> result = stream.collect(Collectors.toList());
@Override
public <R, A> R collect(Collector<? super T, A, R> collector) {
    A container = collector.supplier().get();
    BiConsumer<A, ? super T> accumulator = collector.accumulator();

    this.sequential().forEach(t -> accumulator.accept(container, t));

    return collector.finisher().apply(container);
}
// Collector에서 제공하는 supplier, accumulator, finisher를 사용하여 데이터를 모은다.
// 각 요소가 스트림을 흘러가면서 accumulator를 통해 컨테이너(ArrayList, Map, IntSummaryStatistics 등)에 추가
// finisher가 결과를 변환 (예: 불변 리스트로 변환 등)
```
- Sink 체인 실제 동작
```java
list.stream()
        .filter(x -> x.length() > 3)
        .map(String::toUpperCase)
        .collect(Collectors.toList());
// SourceSpliterator → SinkFilter → SinkMap → SinkCollector
/*
for each element in source:
    SinkFilter.accept(element)
        ↓
        if (predicate == true)
            SinkMap.accept(element)
                ↓
                downstream.accept(mapper.apply(element))
                    ↓
                    SinkCollector.accept(transformedElement)
 */
/*
1. stream() 호출 → ReferencePipeline.Head 생성
2. filter(), map() 호출 → StatelessOp 추가
3. sorted(), distinct() → StatefulOp 추가
4. collect() 호출 → TerminalOp 실행
5. pipeline.evaluate() 실행:
     ↓
     Sink 체인 구성 (filter → map → collector)
     ↓
     소스 Spliterator가 각 요소를 순회
     ↓
     각 Sink.accept() 호출을 통해 데이터 전달
     ↓
     최종 Collector 결과 반환
 */
```

| 구분 | 예시 연산 | 처리 방식 | 데이터 흐름 |
| -- | ----- | ----- | ------ |
| Stateless | `filter()`, `map()`, `peek()` | 각 요소가 들어올 때 즉시 다음으로 전달 | “즉시 전달형” |
| Stateful | `sorted()`, `distinct()`, `limit()`, `skip()` | 일부 또는 전체 요소를 모아두었다가 한꺼번에 처리 | “버퍼링형” |

- 스트림은 두 가지 타입의 파이프라인 노드를 가진다.
```java
abstract class ReferencePipeline<P_IN, P_OUT> {
    ...
    static abstract class StatelessOp<P_IN, P_OUT> extends ReferencePipeline<P_IN, P_OUT> { ... }
    static abstract class StatefulOp<P_IN, P_OUT> extends ReferencePipeline<P_IN, P_OUT> { ... }
}
```
- StatefulOp의 핵심: “전체 또는 일부 버퍼링”
- Stateful 연산은 모든 요소를 잠시 저장해야만 결과를 계산할 수 있는 연산
  - sorted() → 전체 데이터를 다 모아야 정렬 가능
  - distinct() → 지금까지 본 요소를 기억해야 중복 제거 가능
  - limit(5) → 5개까지만 전달하므로 내부적으로 카운팅 필요
- Stateless처럼 “하나 들어오면 바로 다음 Sink로 넘김”이 불가
- sorted()의 내부 흐름
```java
List<String> result = list.stream()
        .filter(s -> s.length() > 2)
        .sorted()
        .map(String::toUpperCase)
        .collect(Collectors.toList());
// filter() — Stateless → 들어올 때마다 통과
// sorted() — Stateful → 모아두었다가 한 번에 방출
// map() — Stateless → 바로 통과
// collect() — 최종 연산
//내부 루프 (의사코드)
// 소스 Spliterator로부터 요소를 순회
List<String> buffer = new ArrayList<>();
for (String element : source) {
    // 1. filter 단계 (Stateless)
    if (element.length() > 2) {
    // 2. sorted 단계 (Stateful) — 일단 모아둠
        buffer.add(element);
    }
}
// 3. sorted가 모든 요소를 모은 후 정렬
Collections.sort(buffer);
// 4. 정렬된 결과를 downstream (map, collect)으로 한 번에 방출
for (String sortedElement : buffer) {
    String mapped = sortedElement.toUpperCase();
    collector.add(mapped);
}
// 5. collect의 finisher()로 결과 반환
return collector.finish();
```
```
┌─────────────────────────────┐
│     Source (Spliterator)    │
└──────────────┬──────────────┘
               │ 요소 하나씩 전달
               ▼
┌─────────────────────────────┐
│ filter (StatelessOp)        │
│  → 조건 통과 시 다음으로   │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ sorted (StatefulOp)         │
│  → 요소를 내부 버퍼에 저장 │
│  → upstream 종료 후 정렬   │
│  → 정렬된 결과를 downstream에 전달 │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ map (StatelessOp)           │
│  → 각 요소 변환 후 collect에 전달 │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│ collect (TerminalOp)        │
│  → 리스트나 맵으로 수집     │
└─────────────────────────────┘
```
- distinct() 내부 루프 (Set 이용)
```java
Set<Object> seen = new HashSet<>();
for (T element : source) {
    if (seen.add(element)) { // add()가 true면 처음 본 값
        downstream.accept(element);
    }
}
```
- limit(n) 내부 루프 (단락 + 상태)
```java
int count = 0;
for (T element : source) {
    if (count++ < n) {
        downstream.accept(element);
    } else {
        break; // short-circuit (조기 종료)
    }
}
// limit()은 완전한 Stateful은 아니지만,
// 요소 개수를 세는 지역 상태(count)를 유지하기 때문에 StatefulOp으로 분류
```
- StatefulOp은 데이터를 멈추게 한다.
- filter/map처럼 바로바로 흘러가지 않고, 데이터를 일단 모아서 처리해야만 하는 구조
- 이 때문에 평가 루프가 분리되고, fusion이 깨지며, 단락(short-circuit)도 제한적