- 기본 함수형 인터페이스:
- 251028-java-adv3/src/lambda3/UtilMain.java
- Function, Consumer, Supplier, Runnable
- 특화 함수형 인터페이스:
- 251028-java-adv3/src/lambda3/UtilMain2.java
- Predicate, Operator (UnaryOperator, BinaryOperator)
- 기타 함수형 인터페이스:
- 매개변수가 2개면 BiXxx를 사용한다.
- 매개변수 3개 이상은 제공하지 않는다. 직접 만들어서 쓴다.
- 기본타입 지원 인터페이스 제공한다. IntFunction 등
# 5. 람다 활용
- 메서드 시그니처(반환 타입, 매개변수)에서 사용하는 모든 타입 파라미터는
- static <여기> 또는 public <여기>에 선언해야 한다.
- `static <T>` (메서드 선언부): "이 메서드는 T라는 타입 파라미터를 사용할 거야"라고 먼저 선언하는 부분
- `List<T>` (반환 타입): 선언된 T를 실제로 사용하는 부분
- `<T>`를 빼면 컴파일러가 "T가 뭔데"라고 에러를 낸다. T를 어디서도 정의하지 않았기 때문
- 만약 클래스 레벨에서 제네릭을 선언했다면 `<T>` 생략 가능
- static 메서드는 인스턴스 없이 호출되기 때문에 클래스의 제네릭 타입을 사용할 수 없다.
- 메서드 자체에서 제네릭을 선언해야 한다.
```java
public class GenericFilter {
    public static <T> List<T> filter(List<T> list, Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T t : list) {
            if (predicate.test(t)) {
                result.add(t);
            }
        }
        return result;
    }
}
public class GenericFilter2 {
    public static <T, R> List<R> filterAndMap(
            List<T> list,
            Predicate<T> predicate,
            Function<T, R> mapper // T를 R로 변환
    ) {
        List<R> result = new ArrayList<>();
        for (T t : list) {
            if (predicate.test(t)) {
                result.add(mapper.apply(t)); // T를 R로 변환해서 추가
            }
        }
        return result;
    }
}
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
List<Integer> lengths = GenericFilter2.filterAndMap(
    names,
    s -> s.length() > 3, // 길이가 3보다 큰 것만
    String::length // String을 Integer로 변환
); // [5, 7] (Alice, Charlie의 길이)
```
## 맵 = 매핑
- 매핑: 대응, 변환을 의미한다.
- 251028-java-adv3/src/lambda4/MapMain.java
- 앞서 제네릭 메서드를 다뤘는데 람다식은 제네릭 메서드를 직접 구현할 수 없다.
- 자바의 람다식은 구현 대상 메서드의 시그니처가 구체적인 타입으로 확정되어 있어야 매핑된다.
- 즉, 컴파일러가 이 람다는 어떤 타입의 함수라고 추론하려고 해도 제네릭 메서드에서는 할 수 없다.
- 제네릭 메서드는 람다식이 필요로 하는 시그니처가 호출 시점까지 정해지지 않기 때문에 매칭이 불가능하다.
- 람다에서 제네릭을 쓰고 싶다면, 제네릭을 메서드가 아니라 인터페이스 수준에서 선언해야 한다.
```java
ListMapInterface<String, Integer> one = (inputList) -> {
    List<Integer> result = new ArrayList<>();
    for (String t : inputList) {
        result.add(Integer.parseInt(t));
    }
    return result;
};
// 타입이 인터페이스에 걸려 있어서 컴파일러가 람다식과 정확히 매칭할 수 있다.
```
- 아니면 제네릭 메서드 구현에 람다 대신 익명 클래스를 쓴다.
```java
ListMapInterface2 two = new ListMapInterface2() {
    @Override
    public <T, R> List<R> listMap(List<T> list) {
        List<R> result = new ArrayList<>();
        for (T t : list) {
            result.add((R) Integer.valueOf(t.toString()));
        }
        return result;
    }
};
```
```java
public class MapMain2 {
    public static void main(String[] args) {
        InnerMap innerMap = new InnerMap();
        List<String> list = List.of("-3", "a", "-1", "1", "2", "b", "5");
        List<Integer> result = innerMap.method(
                list,
                s -> {
                    try {
                        Integer.parseInt(s);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                str -> Integer.parseInt(str)
        );
        System.out.println(result); // [-3, -1, 1, 2, 5]
    }
    private static class InnerMap {
        <T, R> List<R> method(
                List<T> list,
                Predicate<T> predicate,
                Function<T, R> function
        ) {
            List<R> result = new ArrayList<>();
            for (T t : list) {
                if (predicate.test(t)) {
                    result.add(function.apply(t));
                }
            }
            return result;
        }
    }
}
```
- 왜 innerMap.method에서는 제네릭 타입 추론이 가능한가
- method는 제네릭 메서드다. T와 R은 메서드가 호출될 때마다 컴파일러가 추론한다.
```java
// 컴파일러는 method의 인자들과 반환 타입을 동시에 보고 T와 R을 추론한다.
List<Integer> result = innerMap.method(
    list,
    s -> { ... },
    str -> Integer.parseInt(str)
);
```
- ListMapInterface2는 람다가 인터페이스를 직접 구현해야 했다.
- 이 람다가 어떤 타입인지를 인터페이스만 보고 추론해야 했다.
- innerMap.method에서는 람다가 메서드 호출의 인자로 전달되고 있어서
- 컴파일러가 다른 인자들을 보고 람다의 타입을 유추할 수 있다.
- 즉, 인터페이스 정의 시점 → 람다 타입을 추론 불가하고
- 메서드 호출 시점 → 람다 타입을 인자들 보고 추론 가능하다.
- 왜 InnerMap을 static으로 만들어야 main에서 인스턴스 생성이 가능한가
- 이건 정적(static) 내부 클래스 개념
- 비정적 내부 클래스 (Inner class) - 바깥 클래스의 인스턴스에 종속됨
- 정적 내부 클래스 (Static nested class)- 바깥 클래스의 인스턴스 없이 독립적으로 존재 가능
- Non-static inner class 'InnerMap' cannot be referenced from a static context
- main처럼 static 영역을 참조해서는 InnerMap 객체를 못 만든다는 뜻이다.
## 명령형 프로그래밍, 선언적 프로그래밍

| 구분 | 명령형 프로그래밍 (Imperative) | 선언형 프로그래밍 (Declarative) |
| -- |----------------| --------------- |
| 핵심 질문 | 어떻게(how) 수행할까? | 무엇(what)을 수행할까? |
| 개발자 | 프로그램이 목표 달성을 위해 어떤 단계들을 거쳐야 하는지(절차) 명시 | 프로그램이 결과적으로 무엇을 얻고 싶은지(목표) 선언 |
| 제어 흐름 (Control flow) | 개발자가 직접 루프, 조건문, 변수 갱신 등 흐름을 제어 | 시스템이 내부적으로 최적의 흐름을 알아서 수행 |
| 대표 예시 | Java, C, Python(일반 문법) | SQL, HTML, Stream API, React JSX, Haskell |

- 명령형
```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);
List<Integer> evens = new ArrayList<>();
for (Integer n : numbers) {
    if (n % 2 == 0) {
        evens.add(n);
    }
}
// 개발자가 직접 제어 흐름을 기술함
// 명령형은 어떻게 할지(how)를 프로그래머가 명시
// 명령형은 “상태(state) 변화 중심”
```
- 선언형
```java
List<Integer> evens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .toList();
// “짝수만 걸러서 새로운 리스트를 만들어라”라고 무엇을 할지만 선언
// 어떻게 반복을 돌릴지는 Stream API가 알아서 처리
// 선언형은 무엇을 할지(what)를 시스템에 선언
// 선언형은 “관계(expression) 중심”
```

| 관점 | 명령형 | 선언형 |
| -- | --- | --- |
| 초점 | 상태 변화, 절차 | 결과, 관계 |
| 추상화 수준 | 낮음 (세부 제어) | 높음 (의도 표현) |
| 실행 방식 | 컴파일러/런타임이 명시된 절차대로 수행 | 시스템이 최적의 절차를 찾아 수행 |
| 대표 기술 | Loop, 조건문 | SQL, Stream, React, LINQ |

- 함수형 인터페이스: 하나의 동작(함수) 표현하는 추상화
- 람다는: 함수형 인터페이스의 구현체(실제 로직)
- 함수형 프로그래밍 스타일: 람다들을 조합해 전체 로직을 “무엇을 할지” 중심으로 표현
- 람다 표현식은 명령형 코드의 한 단위(실행 절차)를 담고 있다.
- 람다 표현식은 선언형 스타일을 지원하기 위한 구현 도구다.
```java
List<Integer> evens = List.of(1, 2, 3, 4, 5)
    .stream()
    .filter(n -> n % 2 == 0)
    .toList();
// .filter(...)가 “무엇을 걸러낼지”를 선언
// 내부적으로 반복, 조건, 리스트 추가는 시스템(Stream)이 알아서 수행
```

## 스트림
- 251028-java-adv3/src/lambda4/MyStream2.java
```
왜 생성자를 private으로
(1) 객체 생성 방식을 제한하고 통제하기 위해
외부에서 new MyStream2(...)로 직접 인스턴스를 만들면
내부 규칙(internalList가 null이면 안 된다든지 등)을 깨뜨릴 수 있습니다.
하지만 of() 안에서는 그 규칙을 검증하거나, 변환 과정을 넣을 수 있습니다.
public static MyStream2 of(List<Integer> integerList) {
    if (integerList == null) {
        throw new IllegalArgumentException("List cannot be null");
    }
    return new MyStream2(integerList);
}
이렇게 안전한 객체 생성 경로를 강제할 수 있습니다.
(2) 의도를 명확히 하고 코드 가독성을 높이기 위해
// ① 일반 생성자
MyStream2 stream = new MyStream2(List.of(1, 2, 3, 4, 5));
// ② 정적 팩토리 메서드
MyStream2 stream = MyStream2.of(List.of(1, 2, 3, 4, 5));
of()라는 이름은 "이 데이터로부터 스트림을 만든다"는 의도가 명확합니다.
Java의 Stream.of(...), List.of(...), Optional.of(...) 같은 표준 API들도
같은 이유로 of()를 많이 씁니다.
```
- 추가로 객체를 생성하기 전에 이미 있는 객체를 찾아서 반환하는 것도 가능
- Integer.valueOf(): -128 ~ 127 범위는 내부에 가지고 있는 Integer 객체를 반환
```java
public class MyStream3<T> {
    private List<T> list;
    private MyStream3(List<T> list) {
        this.list = list;
    }
    public static <S> MyStream3<S> of(List<S> list) {
        return new MyStream3(list);
    }
    public MyStream3<T> filter(Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T t : list) {
            if(predicate.test(t)) {
                result.add(t);
            }
        }
        return MyStream3.of(result);
    }
    public <R> MyStream3<R> mapper(Function<T, R> function) {
        List<R> result = new ArrayList<>();
        for (T t : list) {
            result.add(function.apply(t));
        }
        return MyStream3.of(result);
    }
    public List<T>  toList() {
        return list;
    }
    public void forEach(Consumer<T> consumer) {
        for (T t : list) {
            consumer.accept(t);
        }
    } // 이런 방식을 내부 반복이라고 한다.
    // 반복 처리를 스트림에 위임한다.
    // break;, continue; 등 흐름 제어가 있는 반복은
    // 외부 반복이 더 간결하고 빠르게 이해될 수 있다.
}
```