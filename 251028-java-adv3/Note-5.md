- 스트림은 데이터 소스를 변경하지 않는다. (Immutable)
- 스트림은 재사용이 안 된다.
```java
public static void main(String[] args) {
    Stream<Integer> stream = Stream.of(1, 2, 3);
    // Stream.of(...)는 Java 8 이상에서 제공되는 정적 팩토리 메서드
    // 전달된 요소들을 기반으로 Stream 객체를 생성
    // 제네릭 스트림, 기본형 특화 스트림 있다.
    stream.forEach(System.out::println);
    //stream.forEach(System.out::println); // 스트림이 이미 작동했거나 닫혔다.
    Stream.of(1, 2, 3).forEach(System.out::println); // Stream 생성해서 사용
}
```
## 컬렉션의 스트림
```java
List<Integer> list = Arrays.asList(1, 2, 3);
Stream<Integer> stream = list.stream();
```
- 기존의 컬렉션(List, Set 등)에서 `.stream()` 메서드를 호출해 스트림을 생성
    - 컬렉션의 요소를 기반으로 스트림이 만들어짐
    - 컬렉션은 이미 존재하는 데이터 구조이므로, 스트림은 그 데이터를 참조함
    - 내부적으로 `Spliterator`를 사용하여 요소를 순회함
    - 병렬 스트림도 `.parallelStream()`으로 쉽게 생성 가능
## Stream.of(...)
```java
Stream<Integer> stream = Stream.of(1, 2, 3);
```
- 개별 요소들을 직접 전달하여 스트림을 생성
    - 컬렉션 없이도 스트림을 만들 수 있음
    - 내부적으로 `Arrays.stream(...)`을 호출하여 배열 기반 스트림을 생성
    - 간단한 테스트나 임시 데이터 처리에 유용
- 참고: 지연 연산은 미룬다. 바로 실행 연산을 즉시(Eager) 연산이라고 한다.
# 9. 스트림 API - 기능
## 스트림 생성 방법
### 컬렉션에서 생성
```java
List<String> list = List.of("A", "B", "C");
// 순차 스트림
Stream<String> stream = list.stream();
// 병렬 스트림
Stream<String> parallelStream = list.parallelStream();
// 내부적으로 Spliterator를 이용해 요소를 순회
// 병렬 스트림은 ForkJoinPool.commonPool()을 사용
```
### 배열에서 생성
```java
String[] arr = {"A", "B", "C"};
Stream<String> stream = Arrays.stream(arr);
int[] nums = {1, 2, 3, 4, 5};
IntStream intStream = Arrays.stream(nums, 1, 4); // 2, 3, 4
```
### 스트림 클래스의 정적 메서드로 생성
```java
Stream<String> stream = Stream.of("A", "B", "C");
Stream<Integer> emptyStream = Stream.empty();
```
### 무한 스트림- 반복적이거나 랜덤한 값을 계속 생성
```java
// generate: Supplier<T>를 이용
Stream<Double> randoms = Stream.generate(Math::random);
// iterate: 초기값과 함수를 이용
Stream<Integer> evenNumbers = Stream.iterate(0, n -> n + 2);
// iterate(Predicate, UnaryOperator) — Java 9+
Stream<Integer> limited = Stream.iterate(0, n -> n < 10, n -> n + 2);
// 무한 스트림은 반드시 limit() 같은 short-circuit 연산으로 끊어줘야 한다.
```
### 파일에서 생성
```java
// Files.lines()로 텍스트 파일의 각 라인을 스트림으로 읽을 수 있다.
try (Stream<String> lines = Files.lines(Path.of("data.txt"))) {
    lines.forEach(System.out::println);
}
```
### 원시 타입 스트림
```java
IntStream range = IntStream.range(1, 5); // 1,2,3,4
IntStream rangeClosed = IntStream.rangeClosed(1, 5); // 1,2,3,4,5
// range()와 rangeClosed()는 내부적으로 Spliterator.OfInt를 구현한 객체를 반환
// Boxing/Unboxing 비용을 줄이기 위해 원시 타입 스트림을 사용하는 것이 좋다.
```
### Builder 패턴 - 요소를 동적으로 추가해 스트림 생성
```java
Stream<String> stream = Stream.<String>builder()
    .add("A")
    .add("B")
    .add("C")
    .build();
```
## 중간 연산 - peek, takeWhile, dropWhile
### peek()
- 스트림 파이프라인의 각 요소를 흘려보내면서 사이드 이펙트를 수행할 수 있게 해주는 연산
```java
Stream.of("a", "b", "c")
      .peek(s -> System.out.println("Processing: " + s))
      .map(String::toUpperCase)
      .forEach(System.out::println);
// 중간 연산이지만 결과를 소비하지는 않음 → 요소를 그대로 다음 단계로 넘김
// 주로 디버깅 용도로 사용 (map()과 달리 변환을 하지 않음)
// peek()은 내부적으로 ReferencePipeline.PeekOp 클래스로 구현됨
// Sink 체인에서 accept() 호출 시 전달받은 값을 그대로 전달하면서, peek의 Consumer를 한 번 호출함
@Override
public void accept(T t) {
    action.accept(t); // peek에서 전달한 Consumer 수행
    downstream.accept(t); // 다음 연산으로 그대로 전달
}
// 즉, 데이터는 변하지 않고, side-effect만 수행하는 연산
```
### takeWhile(Predicate)
- 조건이 참(true)인 동안만 요소를 가져오고
- 거짓(false)이 되는 순간 스트림 처리를 중단(short-circuit)하는 연산
```java
Stream.of(1, 2, 3, 4, 1, 2)
      .takeWhile(n -> n < 4)
      .forEach(System.out::println); // 출력: 1, 2, 3
// 정렬된(stream ordered) 스트림에서 유의미함 (순서를 보장)
// 조건이 처음으로 false가 되는 순간 이후의 요소는 모두 무시
// short-circuit 연산이라 전체 요소를 다 평가하지 않음
// 내부적으로 TakeWhileOp → StatefulOp으로 구현
// forEachRemaining() 시 루프를 돌며 predicate를 검사하고, false가 나오면 바로 중단
// 병렬 스트림의 경우, 데이터 분할 후 처음 false 조건이 나온 split 이후는 평가하지 않음
while (spliterator.tryAdvance(e -> {
    if (predicate.test(e)) {
        sink.accept(e);
    } else {
        stop = true;
    }
})) {
    if (stop) break;
}
// 즉, 앞에서부터 조건을 만족하는 연속된 요소만 통과시키는 구조
```
### dropWhile(Predicate)
- 조건이 참(true)인 동안은 건너뛰고
- 처음으로 false가 되는 시점부터 남은 모든 요소를 통과시킴
```java
Stream.of(1, 2, 3, 4, 1, 2)
        .dropWhile(n -> n < 4)
        .forEach(System.out::println); // 출력: 4, 1, 2
```
- takeWhile과 동일하게 ordered 스트림에서만 의미가 있음
- 한 번 predicate가 false가 되면, 그 이후로는 모든 요소를 그대로 전달
- stateful 중간 연산 (즉, 앞선 상태에 따라 동작이 달라짐)
```java
// 내부적으로 DropWhileOp → StatefulOp 으로 구현
// 내부 flag(stillDropping)를 유지하며
// 처음으로 predicate가 false가 될 때까지 요소를 버림
if (stillDropping) {
    if (!predicate.test(t)) {
        stillDropping = false;
        downstream.accept(t);
    }
} else {
    downstream.accept(t);
}
```
### 병렬 스트림의 일반 원리
- 병렬 스트림은 내부적으로 Spliterator를 이용해
- 데이터를 여러 sub-spliterator로 분할(split)하고
- 각 분할을 ForkJoinPool의 작업 단위(task)로 병렬 처리
```java
List<Integer> list = List.of(1,2,3,4,5,6,7,8);
list.parallelStream().map(...).forEach(...);
// Spliterator가 리스트를 여러 조각으로 쪼갬
// 각각의 조각을 다른 스레드에서 처리
// 마지막에 결과를 결합(combine)
// map, filter 같은 stateless 연산에서는 병렬화 ok
// takeWhile, dropWhile 같은 stateful 연산에서는 문제
```
### takeWhile() 병렬 스트림
- 처음 false가 나온 지점 이후는 전부 버린다는 규칙 때문에
- 병렬 스트림에서는 순서가 있는 데이터에서 부분 중단을 정확히 계산하기 어렵다.
```java
Stream.of(1, 2, 3, 4, 1, 2)
        .parallel()
        .takeWhile(n -> n < 4)
        .forEachOrdered(System.out::println);
// 병렬로 나눠진 여러 청크(chunk)들을 각각 평가
// 각 청크는 “내 부분에서 false가 나왔는지”를 검사
// 모든 청크의 결과를 정렬 순서대로 병합하면서
// 처음으로 false가 발견된 그 위치 이후의 청크는 모두 버림
// 즉, 실제로는 모든 청크를 검사해야 최종 false 위치를 알 수 있으므로
// 완전한 short-circuit은 불가
// 대신 결합(combine) 시점에 이후 데이터를 제거
```
- takeWhile()은 순서가 있는 스트림(ordered stream)에서만 의미가 있고
- 병렬 스트림에서는 거의 순차적으로 동작
### dropWhile() 병렬 스트림
```java
Stream.of(1, 2, 3, 4, 1, 2)
        .parallel()
        .dropWhile(n -> n < 4)
        .forEachOrdered(System.out::println);
// 병렬 청크들이 독립적으로 predicate를 평가
// 각 청크는 아직 드롭 중인지, 이미 false가 나왔는지를 모름
// 따라서 모든 청크를 평가하고 나서
// 정렬 순서를 보장하면서 false 이후 청크부터만 출력하는 식으로 동작
// dropWhile도 병렬 스트림에서는 short-circuit이 실효x
```
## 중간 연산 - FlatMap
### map — 요소를 하나씩 변환
- map은 스트림의 각 요소에 함수를 적용해서 새로운 스트림을 생성
- 스트림의 각 요소를 다른 형태로 1:1 변환한다.
```java
List<String> names = Arrays.asList("alice", "bob", "charlie");
// 각 이름을 대문자로 변환
List<String> upperNames = names.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
// 결과: ["ALICE", "BOB", "CHARLIE"]
// 각 이름의 길이로 변환
List<Integer> lengths = names.stream()
    .map(String::length)
    .collect(Collectors.toList());
// 결과: [5, 3, 7]
```
### flatMap — 중첩된 스트림을 펼쳐서 하나로
- flatMap은 각 요소를 스트림으로 변환한 뒤, 그 스트림들을 하나로 평탄화(flatten)
- 각 요소를 여러 요소로 펼칠 때 사용: 중첩된 구조를 평평하게 만든다.
```java
List<List<Integer>> nested = Arrays.asList(
        Arrays.asList(1, 2),
        Arrays.asList(3, 4),
        Arrays.asList(5, 6)
);
// 중첩된 리스트를 하나의 리스트로 평탄화
List<Integer> flat = nested.stream()
        .flatMap(list -> list.stream())
        .collect(Collectors.toList());
// 결과: [1, 2, 3, 4, 5, 6]
// 문자열을 문자 단위로 분리
List<String> words = Arrays.asList("Hello", "World");
List<String> letters = words.stream()
        .flatMap(word -> Arrays.stream(word.split("")))
        .collect(Collectors.toList());
// 결과: ["H", "e", "l", "l", "o", "W", "o", "r", "l", "d"]
```

| 기능 | map | flatMap |
|------|-----|---------|
| 반환 타입 | 스트림의 스트림이 될 수 있음 | 하나의 평탄화된 스트림 |
| 사용 목적 | 요소 변환 | 중첩 구조를 펼쳐서 처리 |
| 예시 | `Stream<Stream<T>>` 가능 | `Stream<T>`로 평탄화됨 |

- map: `Stream<T> → Stream<R>` (1개 → 1개)
- `Stream<String> → map(String::toUpperCase) → Stream<String>`
- flatMap: `Stream<T> → Stream<R>` (1개 → 0개 이상)
- `Stream<List<String>> → flatMap(List::stream) → Stream<String>`
- 둘 다 스트림의 요소를 하나씩 순회한다.
- 각 요소를 처리한 결과가 어떻게 나오는가가 다르다.
```
/* map - 1개를 1개로 */
["apple", "banana", "cherry"]
// map으로 각각을 대문자로 변환
"apple" → "APPLE"
"banana" → "BANANA"  
"cherry" → "CHERRY"
["APPLE", "BANANA", "CHERRY"]
/* flatMap - 1개를 여러 개로 펼침 */
["Hi there", "Hello", "Good morning"]
// flatMap으로 각각을 단어 단위로 쪼갬
"Hi there" → ["Hi", "there"]
"Hello" → ["Hello"]
"Good morning" → ["Good", "morning"]
["Hi", "there", "Hello", "Good", "morning"]
/* Stream<List<String>> → flatMap(List::stream) */
List<String> list1 = Arrays.asList("Hi", "there"); // [Hi, there]
List<String> list2 = Arrays.asList("Hello"); // [Hello]
List<String> list3 = Arrays.asList("Good", "morning"); // [Good, morning]
List<List<String>> listsOfWords = Arrays.asList(list1, list2, list3); // [[Hi, there], [Hello], [Good, morning]]
Stream<List<String>> stream = listsOfWords.stream();
/* flatMap(List::stream) → Stream<String> */
listsOfWords.stream()
        .flatMap(list -> list.stream()) // 각 리스트를 stream으로 만들고, 그것들을 하나로 합침
        .forEach(System.out::println);
//Hi
//there
//Hello
//Good
//morning
```
## 최종 연산
- collect: Collector를 사용해 결과 수집 (다양한 형태로 변환 가능) - `.collect(Collectors.toList())`
- toList: 스트림을 불변 리스트로 수집 - `.toList()`
- toArray: 스트림을 배열로 변환 - `.toArray(Integer[]::new)`
- forEach: 각 요소에 대해 동작 수행 (반환값x) - `.forEach(System.out::println)`
- count: 요소 개수 반환 - `.count()`
- reduce: 누적 함수 사용해 모든 요소를 단일 결과로 합침
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
// 합계 구하기
int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b); // 0 + 1 + 2 + 3 + 4 + 5 = 15
System.out.println(sum); // 15
// Integer::sum으로 간단하게
int sum2 = numbers.stream()
    .reduce(0, Integer::sum);
// 초기값 없으면 Optional 반환 (빈 스트림일 수 있으니까)
Optional<Integer> sum3 = numbers.stream()
    .reduce((a, b) -> a + b);
System.out.println(sum3.get()); // 15
/*
초기값: 0
0 + 1 = 1
1 + 2 = 3
3 + 3 = 6
6 + 4 = 10
10 + 5 = 15
 */
```
- min/max: 최솟값, 최댓값을 Optional로 반환
```java
List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9);
// 최솟값
Optional<Integer> min = numbers.stream()
        .min(Integer::compareTo);
System.out.println(min.get()); // 1
// 최댓값
Optional<Integer> max = numbers.stream()
        .max(Integer::compareTo);
System.out.println(max.get()); // 9
// 객체에서 사용
List<Student> students = Arrays.asList(
        new Student("김", 85),
        new Student("이", 92),
        new Student("박", 78)
);
// 점수가 가장 높은 학생
Optional<Student> topStudent = students.stream()
        .max((s1, s2) -> Integer.compare(s1.score, s2.score));
```
- findFirst: 조건에 맞는 첫 번째 요소를 Optional로 반환
- findAny: 조건에 맞는 아무 요소나 Optional로 반환
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");
// findFirst: 순서 보장 (첫 번째 요소)
Optional<String> first = names.stream()
        .findFirst();
System.out.println(first.get()); // Alice
// findFirst: 순서 보장 (첫 번째 요소)
Optional<String> firstB = names.stream()
        .filter(name -> name.startsWith("B"))
        .findFirst();
System.out.println(firstB.get()); // Bob
// findAny: 순서 상관없음 (병렬 처리 시 더 빠름)
Optional<String> any = names.parallelStream()
        .filter(name -> name.length() > 3)
        .findAny(); // Alice, Charlie, David 중 아무거나
```
- anyMatch: 하나라도 조건을 만족하는지 boolean 반환
    - anyMatch는 하나만 찾으면 바로 중단
- allMatch: 모두 조건을 만족하는지 boolean 반환
- nonMatch: 한 개도 조건을 만족하지 않으면 true
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
// 하나라도 짝수가 있는지
boolean hasEven = numbers.stream()
    .anyMatch(n -> n % 2 == 0);
System.out.println(hasEven); // true (2, 4가 있음)
// 모두 양수인지
boolean allPositive = numbers.stream()
    .allMatch(n -> n > 0);
System.out.println(allPositive); // true
// 음수가 하나도 없는지
boolean noNegative = numbers.stream()
    .noneMatch(n -> n < 0);
System.out.println(noNegative); // true
// 실전 예시
List<String> emails = Arrays.asList("a@test.com", "b@test.com", "c@test.com");
// 모든 이메일이 유효한지
boolean allValid = emails.stream()
    .allMatch(email -> email.contains("@"));
// 스팸 이메일이 하나라도 있는지
boolean hasSpam = emails.stream()
    .anyMatch(email -> email.contains("spam"));
```
### Optional에서 값 꺼내기
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
// 1. get() - 값이 있으면 꺼내기
Optional<Integer> minOptional = numbers.stream().min(Integer::compareTo);
int min = minOptional.get(); // 1
// 값이 없으면 NoSuchElementException 발생
// 2. orElse() - 값이 없으면 기본값 사용
int min2 = numbers.stream()
    .min(Integer::compareTo)
    .orElse(0); // 값이 없으면 0 반환
// 3. orElseGet() - 값이 없으면 함수 실행
int min3 = numbers.stream()
    .min(Integer::compareTo)
    .orElseGet(() -> {
        System.out.println("값이 없어요");
        return -1;
    });
// 4. orElseThrow() - 값이 없으면 예외 발생
int min4 = numbers.stream()
    .min(Integer::compareTo)
    .orElseThrow(() -> new RuntimeException("값이 없습니다!"));
// 값이 있는지 확인하고 사용 - isPresent()로 확인
Optional<Integer> max = numbers.stream().max(Integer::compareTo);
if (max.isPresent()) {
    int maxValue = max.get();
    System.out.println("최댓값: " + maxValue);
}
```
## 기본형 특화 스트림
- IntStream, LongStream, DoubleStream
- sum(): 모든 요소의 합계를 구한다.
```java
int total = IntStream.of(1, 2, 3).sum();
```
- average(): 모든 요소의 평균을 구해 OptionalDouble로 반환
```java
double avg = IntStream.range(1, 5).average().getAsDouble();
```
- summaryStatistics()
```java
// 최솟값, 최댓값, 합계, 개수, 평균 등이 담긴
// Int/Long/DoubleSummarayStatistics 객체 반환
IntSummaryStatistics stats = IntStream.range(1, 5).summaryStatistics();
```
- mapToLong(), mapToDouble(): 타입 변환
```java
LongStream ls = IntStream.of(1, 2).mapToLong(i -> i * 10L);
```
- mapToObj(): 객체 스트림으로 변환 (기본형 -> 참조형)
```java
Stream<String> s = IntStream.range(1, 5).mapToObj(i -> "this is" + i);
```
- boxed(): 기본형 -> Wrapper 객체 스트림으로 변환
```java
Stream<Integer> i = IntStream.range(1, 5).boxed();
```
- sum(), min(), max(), count(): 합계, 최솟값, 최댓값, 개수를 반환 (타입 유지)