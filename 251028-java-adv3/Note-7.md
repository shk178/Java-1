# 11. Optional
- null 참조를 다루면서 발생하는 문제들이 많았다.
```java
// 전통적인 null 체크 방식
if (user != null) {
    if (user.getAddress() != null) {
        if (user.getAddress().getCity() != null) {
            return user.getAddress().getCity();
        }
    }
}
return "Unknown";
```
- Optional은 "값이 있을 수도, 없을 수도 있다"는 것을 명시적으로 표현하는 컨테이너 객체
```java
// Optional 생성
Optional<String> optional = Optional.of("Hello");
Optional<String> empty = Optional.empty();
Optional<String> nullable = Optional.ofNullable(null); // null 가능
// 값 확인 및 추출
if (optional.isPresent()) {
    System.out.println(optional.get());
}
// 더 나은 방식들
optional.ifPresent(System.out::println);
String result = optional.orElse("default");
String result2 = optional.orElseGet(() -> "computed default");
```
- 명시적인 의도 표현: 메서드 시그니처만 봐도 값이 없을 수 있다는 걸 알 수 있다.
```java
// null을 반환할 수 있다는 게 명확하지 않음
public User findUser(String id) { ... }
// 값이 없을 수 있다는 게 명확함
public Optional<User> findUser(String id) { ... }
```
- 함수형 스타일 지원: map, flatMap, filter 등으로 체이닝 가능
```java
Optional<User> user = findUser(id);
String cityName = user
    .map(User::getAddress)
    .map(Address::getCity)
    .orElse("Unknown");
```
- null 체크 강제: Optional을 사용하면 개발자가 값이 없는 경우를 고려해서 작성한다.
## 주의사항
- 필드로 사용하지 말 것: Optional은 반환 타입으로만 사용 권장
```java
public class User {
    private Optional<String> name; // 필드로 사용
    private Optional<Address> address;
}
//직렬화 불가: Optional은 Serializable을 구현하지 않아서 직렬화 시 문제 발생
//메모리 낭비: Optional 객체 자체가 추가 메모리를 차지
//설계 의도 위배: Optional은 반환 타입으로 설계됨
//null 문제 이중화: Optional<String> name = null 같은 상황이 발생할 수 있어 오히려 더 복잡
// 좋은 예
public class User {
    private String name; // null 가능하다고 문서화
    private Address address;
    // 반환 타입으로만 Optional 사용
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
    public Optional<Address> getAddress() {
        return Optional.ofNullable(address);
    }
}
```
- 컬렉션에는 사용하지 말 것: 빈 컬렉션을 반환 권장
```java
public Optional<List<User>> getUsers() {
    List<User> users = repository.findAll();
    return Optional.ofNullable(users);
}
//불필요한 복잡성: 컬렉션은 이미 "비어있음"을 표현할 수 있다.
//일관성 문제: Optional.empty()와 빈 리스트 둘 다 "없음"을 의미하게 됨
//처리 복잡도 증가: 사용하는 쪽에서 두 가지 경우를 모두 고려해야 함
// 좋은 예
public List<User> getUsers() {
    List<User> users = repository.findAll();
    return users != null ? users : Collections.emptyList();
}
// 사용하는 쪽이 단순해짐
List<User> users = getUsers();
if (!users.isEmpty()) {
    // ...
}
// 더 좋은 예: Stream 반환도 고려
public Stream<User> getUsers() {
    return repository.findAll().stream();
}
// 또는
public List<User> getUsers() {
    return List.copyOf(repository.findAll()); // 불변 리스트
}
```
- get() 직접 호출 주의: isPresent() 없이 get()을 호출하면 NoSuchElementException 발생 가능
```java
//NullPointerException 대신 NoSuchElementException을 얻을 뿐
// 이것도 권장하지 않음
if (userOpt.isPresent()) {
    User user = userOpt.get();
    // ...
}
// 이건 그냥 null 체크와 다를 바 없음
// 좋은 예
// orElse 사용
User user = userOpt.orElse(defaultUser);
// orElseGet 사용 (lazy evaluation)
User user = userOpt.orElseGet(() -> createDefaultUser());
// orElseThrow 사용 (명확한 예외)
User user = userOpt.orElseThrow(() ->
        new UserNotFoundException("User not found: " + id)
);
// ifPresent 사용
userOpt.ifPresent(user -> {
    System.out.println(user.getName());
    sendEmail(user);
});
// ifPresentOrElse (Java 9+)
userOpt.ifPresentOrElse(
    user -> System.out.println("Found: " + user.getName()),
    () -> System.out.println("User not found")
);
// map/flatMap 체이닝
String userName = userOpt
        .map(User::getName)
        .map(String::toUpperCase)
        .orElse("UNKNOWN");
// orElse - 항상 평가됨
User user = userOpt.orElse(createDefaultUser()); // 값이 있어도 createDefaultUser() 호출
// orElseGet - 필요할 때만 평가됨
User user = userOpt.orElseGet(() -> createDefaultUser()); // 값이 없을 때만 호출
```
- Optional을 Optional로 감싸지 말 것
```java
// 하지 말 것
Optional<Optional<User>> user = Optional.of(findUser(id));
// flatMap 사용 권장
Optional<String> city = userOpt.flatMap(User::getAddress)
        .map(Address::getCity);
```
- Optional을 메서드 파라미터로 사용하지 말 것
```java
// 나쁜 예
public void updateUser(Optional<User> user) { ... }
// 좋은 예 - 오버로딩 사용
public void updateUser(User user) { ... }
public void updateUserIfPresent(User user) { ... }
// 또는 null 허용을 명시
public void updateUser(@Nullable User user) { ... }
```
- Optional은 "값이 없을 수 있음"을 API로 명시적으로 표현하기 위함
## Optional 생성 메서드
- Optional.of(T value): value가 null이 아닐 때 사용, null 전달 시 NPE 발생
- Optional.ofNullable(T value): value가 null이거나 null이 아닐 때 사용, null 전달 시 Optional.empty()를 반환
- Optional.empty(): null일 때 사용 (값이 없음을 표현)
- 값이 있을 때 print: `Optional[value]` 출력
- 값이 없을 때 print: `Optional.empty` 출력
## Optional 값 꺼내는 메서드
- isPresent(), isEmpty(): true/false
- get(): value or NSEE
- ofElse(T other): value or other
- orElseGet(Supplier<? extends T> supplier): value or supplier 호출
- orElseThrow(() -> new RuntimeException("err msg")): value or 지정한 예외
- or(Supplier<? extends Optional<? extends T>> supplier): Optional.of(value) or supplier 호출
```java
public Optional<T> or(Supplier<? extends Optional<? extends T>> supplier)
// Optional<T> 인스턴스 or(...)를 호출 때 supplier는 Optional<? extends T>를 반환
// Supplier 객체를 인자로 받는다.
```
## Optional 값 처리하는 메서드
- ifPresent(Consumer<? super T> action)
- ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction)
- map(Function<? super T, ? extends U> mapper): 값이 있으면 mapper를 적용한 결과(`Optional<U>`) 반환, 없으면 Optional.empty() 반환
- flatMap(Function<? super T, ? extends Optional<? extends U>> mapper): Optional을 반환할 때 평탄화해서 반환
```java
// 평탄화란 Optional 안의 값을 꺼내서 함수에 넘기고, 함수가 반환한 Optional을 그대로 반환하는 것
Function<? super T, ? extends Optional<? extends U>>
? super T
// 입력 타입은 T 또는 T의 상위 타입이어야 한다는 뜻
// 예: T가 String이면, Object, CharSequence, String 모두 가능
// 더 일반적인 타입도 받을 수 있게 유연성을 주기 위해서
? extends Optional<? extends U>
// 반환 타입은 Optional<U> 또는 Optional의 하위 타입이어야 한다는 뜻
// 그리고 그 안에 들어 있는 값도 U 또는 U의 하위 타입
// 예: U가 Number면, Optional<Integer>, Optional<Double> 등도 허용
Optional<String> opt = Optional.of("hello");
Optional<Integer> result = opt.flatMap(s -> Optional.of(s.length()));
// opt는 Optional<String>이고, 값 "hello"를 가지고 있음
// flatMap은 내부적으로 "hello"가 존재하는지 확인함
// 존재하면 s -> Optional.of(s.length()) 함수에 "hello"를 넘김
// 함수는 Optional<Integer>를 반환함 → Optional[5]
// flatMap은 그 Optional을 그대로 반환함
```

| 메서드 | 함수에 넘기는 값 | 함수의 반환값 | 최종 결과 |
|--------|------------------|----------------|------------|
| `map` | Optional 안의 값 | `일반 값 or Optional` | `Optional<U> or Optional<Optional<U>>` |
| `flatMap` | Optional 안의 값 | `Optional<U>` | `Optional<U>` (중첩 제거됨) |

- filter(Predicator<? super T> predicate): 값이 있고 조건을 만족하면 그대로 반환, 조건x이거나 값이 없으면 Optional.empty()
- stream(): 값이 있으면 단일 요소를 담은 `Stream<T>` 반환, 없으면 빈 스트림 반환