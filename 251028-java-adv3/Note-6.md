# 10. 스트림 API - 컬렉터
- https://docs.oracle.com/javase/10/docs/api/java/util/stream/Collectors.html
```java
R result = stream.collect(Collector<T, A, R> collector);
// T: 스트림의 요소 타입
// A: 누적기(accumulator)의 중간 타입
// R: 최종 결과 타입
// Collectors 클래스는 Collector의 구현체를 손쉽게 만들기 위한 정적 팩토리 메서드들을 제공
// 스트림의 최종 연산(collect())에서 요소를 수집하거나 변환, 요약, 그룹화할 때 사용되는 유틸리티
```
## 컬렉션으로 변환하기
- toList()는 순서가 있는 리스트
- toSet()은 중복을 제거한 집합
- 특정 구현체가 필요하면 toCollection(생성자)
- toMap()은 각 요소를 키와 값으로 변환한 맵 - 세 번째 파라미터로 충돌(키 중복) 처리 방법을 지정
```java
Collector<T, ?, List<T>> toList()
Collector<T, ?, Set<T>> toSet()
Collector<T, ?, C> toCollection(Supplier<C> collectionFactory)
ArrayList<String> list = stream.collect(Collectors.toCollection(ArrayList::new));
// 기본 형태
Collector<T, ?, Map<K,U>> toMap(
        Function<T, K> keyMapper,      // 키 추출 함수
        Function<T, U> valueMapper     // 값 추출 함수
)
// 중복 키 처리
Collector<T, ?, Map<K,U>> toMap(
        Function<T, K> keyMapper,
        Function<T, U> valueMapper,
        BinaryOperator<U> mergeFunction  // 충돌 시 병합 방법
)
// Map 구현체 지정
Collector<T, ?, M> toMap(
        Function<T, K> keyMapper,
        Function<T, U> valueMapper,
        BinaryOperator<U> mergeFunction,
        Supplier<M> mapFactory           // Map 생성자
)
// TreeMap으로 수집
Map<Integer, String> sortedMap = students.stream()
        .collect(Collectors.toMap(
                Student::getId,
                Student::getName,
                (v1, v2) -> v1,
                TreeMap::new
        ));
// 불변 컬렉션 반환
Collector<T, ?, List<T>> toUnmodifiableList()
Collector<T, ?, Set<T>> toUnmodifiableSet()
List<String> immutableList = stream.collect(Collectors.toUnmodifiableList());
// 이후 add(), remove() 시도하면 UnsupportedOperationException
Collector<T, ?, Map<K,U>> toUnmodifiableMap(
        Function<T, K> keyMapper,
        Function<T, U> valueMapper
)
Collector<T, ?, Map<K,U>> toUnmodifiableMap(
        Function<T, K> keyMapper,
        Function<T, U> valueMapper,
        BinaryOperator<U> mergeFunction
)
Map<Integer, String> immutableMap = students.stream()
        .collect(Collectors.toUnmodifiableMap(
                Student::getId,
                Student::getName
        ));
```
## 문자열 합치기
- joining() 여러 문자열을 하나로 합칠 때 사용 - 구분자/접두사/접미사 추가 가능
```java
Collector<CharSequence, ?, String> joining()
Collector<CharSequence, ?, String> joining(CharSequence delimiter)
Collector<CharSequence, ?, String> joining(
    CharSequence delimiter,
    CharSequence prefix,
    CharSequence suffix
)
// 구분자 + 접두사/접미사
String result = stream.collect(Collectors.joining(", ", "[", "]"));  // "[a, b, c]"
```
## 통계 내기 (집계 연산 메서드 - 숫자 계산)
- counting()은 개수
- summingInt()은 합계
- averagingDouble()은 평균
- summarizingInt()는 한 번의 순회로 여러 통계를 얻을 수 있다.
```java
Collector<T, ?, Long> counting()
// mapper: 숫자 값을 추출하는 함수
Collector<T, ?, Integer> summingInt(ToIntFunction<T> mapper)
Collector<T, ?, Double> averagingInt(ToIntFunction<T> mapper)
Collector<T, ?, IntSummaryStatistics> summarizingInt(ToIntFunction<T> mapper)
Collector<T, ?, LongSummaryStatistics> summarizingLong(ToLongFunction<T> mapper)
Collector<T, ?, DoubleSummaryStatistics> summarizingDouble(ToDoubleFunction<T> mapper)
IntSummaryStatistics stats = students.stream()
        .collect(Collectors.summarizingInt(Student::getAge));
System.out.println(stats.getCount());    // 개수
        System.out.println(stats.getSum());      // 합계
        System.out.println(stats.getMin());      // 최소값
        System.out.println(stats.getMax());      // 최대값
        System.out.println(stats.getAverage());  // 평균
```
## 그룹화
- groupingBy()는 특정 기준으로 데이터를 그룹화
- 예를 들어 학생들을 학년별로 그룹화하면
- 결과는 Map이 되는데
- 키는 학년, 값은 해당 학년의 학생 리스트
- 여기에 다운스트림 컬렉터를 추가하면
- 각 그룹의 개수를 세거나, 평균을 구하거나, 또 다른 그룹화를 할 수도 있다.
- partitioningBy()는 조건에 따라 true/false 두 그룹으로만 나눔
```java
// 기본 - List로 그룹화
Collector<T, ?, Map<K, List<T>>> groupingBy(
    Function<T, K> classifier  // 분류 기준
)
// 다운스트림 컬렉터 지정
Collector<T, ?, Map<K, D>> groupingBy(
    Function<T, K> classifier,
    Collector<T, A, D> downstream  // 각 그룹에 적용할 컬렉터
)
// Map 구현체까지 지정
Collector<T, ?, M> groupingBy(
    Function<T, K> classifier,
    Supplier<M> mapFactory,        // Map 생성자
    Collector<T, A, D> downstream
)
// TreeMap으로 정렬된 그룹화
Map<String, List<Student>> sortedGroups = students.stream()
        .collect(Collectors.groupingBy(
                Student::getGrade,
                TreeMap::new,
                Collectors.toList()
        ));
// 기본 - List로 분할
Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(
        Predicate<T> predicate  // true/false 조건
)
// 다운스트림 컬렉터 지정
Collector<T, ?, Map<Boolean, D>> partitioningBy(
        Predicate<T> predicate,
        Collector<T, A, D> downstream
)
// 각 그룹의 개수
Map<Boolean, Long> counts = students.stream()
        .collect(Collectors.partitioningBy(
                s -> s.getScore() >= 60,
                Collectors.counting()
        ));
```
## 최대/최소값 찾기
- maxBy()와 minBy()는 비교 기준을 정하면
- 가장 큰 값이나 작은 값을 Optional로 반환
- Optional은 스트림이 비어있을 수도 있어서 사용
```java
Collector<T, ?, Optional<T>> maxBy(Comparator<T> comparator)
Collector<T, ?, Optional<T>> minBy(Comparator<T> comparator)
Optional<Student> oldest = students.stream()
        .collect(Collectors.maxBy(Comparator.comparing(Student::getAge)));
Optional<Student> youngest = students.stream()
        .collect(Collectors.minBy(Comparator.comparing(Student::getAge)));
```
## 변환하며 수집하기
- mapping()은 그룹화와 함께 사용할 때 유용
- 그룹화하면서 동시에 각 요소를 다른 형태로 변환할 수 있다.
- 예를 들어 학년별로 그룹화하면서 학생 객체가 아닌 이름만 모을 수 있다.
- collectingAndThen()은 수집 후 한 번 더 변환
- 리스트로 수집한 다음 불변 리스트로 만들거나
- 크기를 확인하거나 하는 추가 작업을 할 수 있다.
```java
Collector<T, ?, R> mapping(
    Function<T, U> mapper,         // 변환 함수
    Collector<U, A, R> downstream  // 변환 후 수집 방법
)
// 학년별 학생 이름 리스트
Map<String, List<String>> namesByGrade = students.stream()
        .collect(Collectors.groupingBy(
                Student::getGrade,
                Collectors.mapping(Student::getName, Collectors.toList())
        ));
Collector<T, A, RR> collectingAndThen(
        Collector<T, A, R> downstream,  // 먼저 수집
        Function<R, RR> finisher        // 수집 후 변환
)
// 리스트 크기 반환
Integer size = stream.collect(
        Collectors.collectingAndThen(
                Collectors.toList(),
                List::size
        ));
// 그룹화 후 각 그룹을 불변 리스트로
Map<String, List<Student>> groups = students.stream()
        .collect(Collectors.groupingBy(
                Student::getGrade,
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList
                )
        ));
```
## 최신 기능들
- filtering()과 flatMapping()
- 그룹화 단계에서 필터링과 평탄화를 할 수 있다.
- 이전에는 스트림 단계에서만 가능했던 작업을
- 다운스트림 컬렉터로도 할 수 있게 됐다.
- teeing() 하나의 스트림을
- 두 개의 다른 컬렉터로 동시에 처리하고
- 그 결과를 합칠 수 있다.
- 한 번의 순회로 두 가지 다른 통계를 동시에 구할 때 유용
```java
Collector<T, ?, R> filtering(
    Predicate<T> predicate,        // 필터 조건
    Collector<T, A, R> downstream  // 필터 후 수집
)
// 학년별 성인 학생만
Map<String, List<Student>> adultsByGrade = students.stream()
        .collect(Collectors.groupingBy(
                Student::getGrade,
                Collectors.filtering(
                        s -> s.getAge() >= 18,
                        Collectors.toList()
                )
        ));
Collector<T, ?, R> flatMapping(
        Function<T, Stream<U>> mapper, // Stream으로 변환
        Collector<U, A, R> downstream  // 평탄화 후 수집
)
// 학년별 모든 취미 수집
Map<String, List<String>> hobbiesByGrade = students.stream()
        .collect(Collectors.groupingBy(
                Student::getGrade,
                Collectors.flatMapping(
                        s -> s.getHobbies().stream(),
                        Collectors.toList()
                )
        ));
Collector<T, ?, R> teeing(
        Collector<T, ?, R1> downstream1,  // 첫 번째 컬렉터
        Collector<T, ?, R2> downstream2,  // 두 번째 컬렉터
        BiFunction<R1, R2, R> merger      // 두 결과 병합
)
// 개수와 평균을 동시에
record Stats(long count, double average) {}
Stats stats = students.stream()
        .collect(Collectors.teeing(
                Collectors.counting(),
                Collectors.averagingDouble(Student::getScore),
                Stats::new
        ));
// 최소값과 최대값을 동시에
record MinMax(Optional<Integer> min, Optional<Integer> max) {}
MinMax minMax = numbers.stream()
        .collect(Collectors.teeing(
                Collectors.minBy(Integer::compare),
                Collectors.maxBy(Integer::compare),
                MinMax::new
        ));
```
## 불변 컬렉션을 만드는 다른 방법
```java
// collectingAndThen 사용
List<String> immutable = stream.collect(
    Collectors.collectingAndThen(
        Collectors.toList(),
        Collections::unmodifiableList
    ));
Set<String> immutableSet = stream.collect(
    Collectors.collectingAndThen(
        Collectors.toSet(),
        Collections::unmodifiableSet
    ));
Map<K, V> immutableMap = stream.collect(
    Collectors.collectingAndThen(
        Collectors.toMap(k -> k, v -> v),
        Collections::unmodifiableMap
    ));
// Guava 라이브러리 사용
ImmutableList<String> immutable = stream.collect(
    ImmutableList.toImmutableList()
);
```
## 다운스트림
- 다운스트림은 "하류", "아래쪽"이라는 뜻
- Collectors에서는 "이후에 추가로 적용되는 컬렉터"를 의미
- 데이터 → 그룹화(상류) → 각 그룹 처리(하류/downstream) → 최종 결과
- 그룹화가 먼저 일어나고, 그 다음에 각 그룹에 대해 추가 작업
- "추가 작업"을 담당하는 컬렉터가 바로 다운스트림 컬렉터
```java
Map<String, List<Student>> byGrade = students.stream()
    .collect(Collectors.groupingBy(Student::getGrade));
// 실제로는 이것과 같음 (다운스트림을 생략하면 기본 다운스트림이 자동으로 적용)
Collectors.groupingBy(Student::getGrade, Collectors.toList())
// 결과:
// "1학년" -> [학생1, 학생2, 학생3]
// "2학년" -> [학생4, 학생5]
// "3학년" -> [학생6, 학생7, 학생8, 학생9]
```
```java
// toMap과 joining에서 생략되는 건 다운스트림이 아니다.
// 2개 파라미터 버전
toMap(keyMapper, valueMapper)
// ↓ 내부적으로 호출
toMap(keyMapper, valueMapper, (v1, v2) -> throw exception)  // 중복 시 에러
// 3개 파라미터 버전  
toMap(keyMapper, valueMapper, mergeFunction)
// ↓ 내부적으로 호출
toMap(keyMapper, valueMapper, mergeFunction, HashMap::new)  // HashMap 사용
// 파라미터 없음
joining()
// ↓ 내부적으로 호출
joining("")  // 빈 문자열 구분자
// 1개 파라미터
joining(", ")
// ↓ 내부적으로 호출
joining(", ", "", "")  // 빈 접두사/접미사
/*
다운스트림은 "Collector 타입의 파라미터"
즉, 또 다른 Collector를 받는 경우에만 다운스트림
- 다운스트림:
    - 타입: Collector<T, A, R>
    - 역할: 추가적인 수집 작업을 정의
    - 또 다른 컬렉터를 중첩해서 사용
    - 예: counting(), toList(), averaging() 등
- 일반 파라미터:
    - 타입: Function, BinaryOperator, String, Supplier 등
    - 역할: 현재 컬렉터의 동작 방식을 설정
    - 컬렉터가 아닌 일반 값이나 함수
 */
```
- 다운스트림: 스트림 최종 연산 메서드에 인자로 받는다.
```java
/*
다운스트림을 받는 메서드들
= 다른 Collector를 받아서 추가 수집 작업을 하는 메서드
 */
// 그룹화한 후, 각 그룹에 downstream을 적용
groupingBy(classifier, Collector downstream)
partitioningBy(predicate, Collector downstream)
// 요소를 변환한 후, 변환된 요소들에 downstream을 적용
mapping(mapper, Collector downstream)
// 필터링한 후, 남은 요소들에 downstream을 적용
filtering(predicate, Collector downstream)
// 평탄화한 후, 결과에 downstream을 적용
flatMapping(mapper, Collector downstream)
// downstream으로 수집한 후, 결과를 변환
collectingAndThen(Collector downstream, finisher)
// 다운스트림 중첩 사용 가능하다.
Map<String, Map<String, Double>> result = students.stream()
        .collect(Collectors.groupingBy(
                Student::getGrade,                    // 1단계: 학년별 그룹화
                Collectors.groupingBy(                // 2단계(다운스트림): 성별로 다시 그룹화
                        Student::getGender,
                        Collectors.averagingDouble(       // 3단계(다운스트림의 다운스트림): 평균
                                Student::getScore
                        )
                )
        ));
// 결과:
// "1학년" -> {
//     "남" -> 80.5,
//     "여" -> 80.2
// }
// "2학년" -> {
//     "남" -> 90.0,
//     "여" -> 90.4
// }
```
## 스트림 구조
```
데이터소스.stream()     // 1. 스트림 생성
    .중간연산()          // 2. 중간 연산 - 지연 실행(lazy) (0개 이상)
    .중간연산()
    .중간연산()
    .collect()          // 3. 최종 연산 - 호출되어야 실제로 스트림이 실행
```
- collect() 없이도 최종 연산 가능
- collect()는 최종 연산 중 하나일 뿐이다.
- 최종 연산은 한 개만, 중간 연산은 여러 개 체이닝 가능
```java
// count() 사용
long count = students.stream()
    .filter(s -> s.getAge() >= 18)
    .count();  // 최종 연산
// forEach() 사용
students.stream()
    .filter(s -> s.getScore() >= 90)
    .forEach(s -> System.out.println(s.getName()));  // 최종 연산
// anyMatch() 사용
boolean hasAdult = students.stream()
    .anyMatch(s -> s.getAge() >= 18);  // 최종 연산
// reduce() 사용
int totalAge = students.stream()
    .map(Student::getAge)
    .reduce(0, Integer::sum);  // 최종 연산
```
```java
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DownStream {
    static class Student {
        private String name;
        private int grade;
        private int score;
        public Student(String name, int grade, int score) {
            this.name = name;
            this.grade = grade;
            this.score = score;
        }
        @SuppressWarnings("unchecked")
        public <T> T get(String fieldName) {
            try {
                Field field = this.getClass().getDeclaredField(fieldName);
                field.setAccessible(true); // private 필드 접근 허용
                return (T) field.get(this); // 현재 객체에서 필드 값 반환
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args) {
        List<Student> students = List.of(
                new Student("K", 1, 85),
                new Student("L", 1, 70),
                new Student("P", 2, 90),
                new Student("H", 2, 70),
                new Student("C", 3, 95),
                new Student("G", 3, 70)
        );
        Map<Integer, List<Student>> gradeGroup = students.stream()
                .collect(Collectors.groupingBy(
                        s -> s.get("grade"),
                        Collectors.toList() // 생략 가능
                        // groupingBy()의 두 번째 인자의 기본값이 Collectors.toList()
                ));
        // groupingBy의 두 번째 파라미터(다운스트림)를 생략하면
        // 컴파일러가 타입을 추론할 때 첫 번째 파라미터만 보고 판단
        Map<Integer, List<String>> gradeName = students.stream()
                .collect(Collectors.groupingBy(
                        //s -> s.get("grade"), // 여기서 s의 타입이 Student인 건 알지만
                        // s.get("grade")의 반환 타입이 Integer인지 컴파일러가 확신할 수 없음
                        //s -> (Integer) s.get("grade"), // 명시적 캐스팅 가능 - 추론 에러 발생
                        // 하지만 제네릭 메서드 대신 일반 getter를 만드는 게 낫다.
                        //s -> s.<Integer>get("grade"), // 타입 힌트도 가능 - 추론 에러 발생
                        s -> s.get("grade"),
                        /* Collectors.groupingBy( // 추론 에러 해결 - mapping으로 바꾼다. */
                        Collectors.mapping(
                                s -> s.get("name"), // 여기까지 쓰면 타입 추론 가능 - 추론 에러 발생
                                Collectors.toList()
                        )
                        /*
                        inference variable D has incompatible equality constraints java.util.List<java.lang.String>, java.util.Map<K,D>
                            D는 groupingBy의 결과 값 타입을 나타내는 타입 변수
                            컴파일러가 D를 List<String>으로 추론하려고 함
                            동시에 D를 Map<K, D>의 일부로도 추론하려고 함
                            이 두 가지가 서로 맞지 않아서 에러 발생
                            캐스팅이나 타입 힌트를 추가해도 문제는 해결되지 않는다.
                            (Integer) s.get("grade") 덕분에 키가 Integer인 건 명확해짐
                            첫 번째 groupingBy:
                                입력: Stream<Student>
                                키: s.get("grade") → Integer
                                값: 두 번째 groupingBy의 결과
                            두 번째 groupingBy (다운스트림):
                                입력: 각 그룹의 Student들
                                키: s.get("name") → String
                                값: Collectors.toList() → List<Student>
                                결과: Map<String, List<Student>>
                            전체 결과가 이렇게 나와버린다: Map<Integer, Map<String, List<Student>>>
                         */
                        /*
                        해결 방법:
                        // 학년 → 학생 이름 리스트
                        // {1=[K, L], 2=[P, H], 3=[C, G]}
                        Map<Integer, List<String>> gradeName = students.stream()
                            .collect(Collectors.groupingBy(
                                s -> s.<Integer>get("grade"),     // 학년별로 그룹화
                                Collectors.mapping(                // Student를 String으로 변환
                                    s -> s.<String>get("name"),
                                    Collectors.toList()
                                )
                            ));
                        //또는 중첩 그룹화를 원한다면 - 학년 → 이름 → 학생 리스트
                        // {1={K=[학생], L=[학생]}, 2={P=[학생], H=[학생]}}
                        Map<Integer, Map<String, List<Student>>> nestedGroup = students.stream()
                            .collect(Collectors.groupingBy(
                                s -> s.<Integer>get("grade"),     // 학년별로
                                Collectors.groupingBy(             // 이름별로 다시 그룹화
                                    s -> s.<String>get("name"),
                                    Collectors.toList()
                                )
                            ));
                        // groupingBy는 항상 Map을 반환
                        // mapping은 요소를 변환
                         */
                ));
        // 제네릭 메서드도 메서드 참조가 가능 - 하지만 타입 파라미터를 명시할 수 없다는 제약
        // 제네릭 메서드 - 람다식에서는 타입 파라미터 명시 가능 s -> s.<String>get("name")
        // 제네릭 메서드 참조 - 컨텍스트에서 타입 추론이 가능하면 작동
        /*
        List<Box<String>> boxes = ...;
        List<String> values = boxes.stream()
                .map(Box::getValue)  // 반환 타입이 String으로 추론됨
                .collect(Collectors.toList());
         */
        // 메서드 참조와 매개변수 - 메서드 참조는 매개변수를 직접 전달할 수 없다.
        // 메서드 참조는 메서드 자체를 가리키는 거지, 메서드 호출이 아니다.
        // 람다식 - 매개변수 전달 가능
        System.out.println(gradeGroup);
        System.out.println(gradeName);
    }
}
//{1=[stream.DownStream$Student@66a29884, stream.DownStream$Student@4769b07b], 2=[stream.DownStream$Student@cc34f4d, stream.DownStream$Student@17a7cec2], 3=[stream.DownStream$Student@65b3120a, stream.DownStream$Student@6f539caf]}
//{1=[K, L], 2=[P, H], 3=[C, G]}
```