# 7. 컬렉션 프레임워크 - 해시
- (1) Set
- 유일한 요소들의 컬렉션
- 중복x, 입/출력 순서 미보장, 요소 유무 확인에 최적화
- 요소 유무 확인의 최적화 = 빠른 유무 검색
- List: 장바구니 목록, 순서가 중요한 이벤트 목록
- Set: 회원ID 집합, 고유 항목 집합
- (2) Set 직접 구현
- 251007-java-mid2/src/hash/hashSet.java
- contains가 add를 O(n)으로 만든다.
- 해시 알고리즘: 중복 확인을 빠르게 만든다.
- 데이터 값 자체를 배열의 인덱스로 사용한다면..
- 검색이 빨라질 것이다. 하지만 메모리 낭비된다.
- 나머지 연산을 활용하면 메모리 절약된다.
- (3) 나머지 연산
- hashIndex(): 해시 인덱스를 반환
- 해시 인덱스는 입력 값을 계산해서 인덱스로 사용하는 것을 말한다.
- 여기서는 입력 값을 배열의 크기로 나머지 연산해서 구한다.
- add(): true(추가함)/false(추가 안 함)를 반환
- 해시 인덱스를 먼저 구한다.
- 구한 해시 인덱스의 위치에 데이터를 저장한다.
- 조회: 해시 인덱스를 구하고, `배열명[해시 인덱스]`로 값을 조회한다.
- 해시가 충돌할 수 있는 한계가 있다.
- (4) 해시 충돌 해결
- CAPACITY를 늘리는 것: 메모리 낭비, 복잡한 구현 등 문제
- 배열 안에 배열 등 만드는 것: 한 인덱스에만 계속 저장되면 문제
- 한 인덱스에 저장되는 (분포가 고르지 않은) 경우는 많지 않다.
- 평균 복잡도는 배열 안에 배열, 리스트 등 만드는 게 낫다.
- 251007-java-mid2/src/hash/hashSet2.java
- 배열이 있고 배열의 요소가 연결 리스트, 연결 리스트의 요소가 Integer인 구조이다.
- list라는 이름보다 buckets(바구니들)라는 이름이 낫다.
- (5) 해시 인덱스 충돌 확률
- 한 인덱스에 데이터가 2개 이상 담길 확률을 말한다.
- 저장할 데이터 수와 배열 크기와 관련이 있다.
- 입력한 데이터 수가 배열 크기 75%를 넘지 않으면 충돌 적다.
- (6) 해시 인덱스 성능
- 데이터 저장: 평균 O(1), 최악 O(n)
- 데이터 조회: 평균 O(1), 최악 O(n)
# 8. 컬렉션 프레임워크 - HashSet
- 1. unchecked 경고
- (1) 제네릭 배열 생성 & 캐스팅
```java
public class HashSet3<E> {
    private LinkedList<E>[] buckets;
    private int capacity;
    private int size;
    @SuppressWarnings("unchecked")
    HashSet3(int capacity) {
        buckets = (LinkedList<E>[]) new LinkedList[capacity];
        //제네릭 배열 직접 생성은 불가
        //new LinkedList[capacity] //먼저 raw type 배열 생성 (타입 불일치)
        //(LinkedList<E>[]) //제네릭 타입 배열로 캐스팅: unchecked 경고 발생
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new LinkedList<>();
        }
        this.capacity = capacity;
        this.size = 0;
    }
}
//객체 생성 시에만 타입 지정
HashSet3<String> set = new HashSet3<>(10);
HashSet3<Integer> numbers = new HashSet3<>(20);
```
- (2) raw type 사용
```java
//제네릭 타입인데 타입 파라미터 없이 사용
List list = new ArrayList(); //raw type
list.add("hello"); //unchecked 경고
List<String> typed = new ArrayList<>(); //경고 없음
```
- (3) raw type과 제네릭 타입 혼용
```java
List rawList = new ArrayList();
List<String> stringList = rawList; //unchecked 경고 (변환)
//반대도 마찬가지
List<Integer> numbers = new ArrayList<>();
List raw = numbers; //경고 없음
raw.add("문자열"); //unchecked 경고 (뭐든 넣을 수 있음)
```
- (4) 제네릭 타입의 instanceof & 캐스팅
```java
Object obj = new ArrayList<String>();
//instanceof는 런타임에 타입 소거되어 검사 불가
if (obj instanceof List<String>) { } //컴파일 에러
if (obj instanceof List) { //가능
    List<String> list = (List<String>) obj; //unchecked 경고
}
```
- (5) 가변인자(Varargs)와 제네릭
```java
//제네릭 가변인자는 내부적으로 배열로 처리됨
public <T> void varargs(T... items) { //unchecked 경고 (힙 오염 가능)
    //items는 T[] 배열
}
//@SafeVarargs로 억제 가능
@SafeVarargs
public final <T> void safeVarargs(T... items) { }
```
- (6) 리플렉션 사용
```java
Class<?> clazz = Class.forName("java.util.ArrayList");
List<String> list = (List<String>) clazz.newInstance(); //unchecked 경고
```
- (7) 제네릭 타입을 알 수 없는 반환값
```java
public List getList() { //raw type 반환
    return new ArrayList<String>();
}
List<String> list = getList(); //unchecked 경고
```
- (8) 공통점
- 컴파일러가 타입 안정성을 검증할 수 없는 상황에서 발생
```
Raw type 사용
제네릭 → Raw 변환
제네릭 배열 관련 작업
런타임 타입 체크/캐스팅
타입 정보가 소거되는 상황
```
- 안전하다고 확신할 때 @SuppressWarnings("unchecked") 사용
- 2. HashSet3
- (1) 생성자에 `<E>`를 쓰지 않기로 정해졌다.
```java
public class HashSet3<E> {
    HashSet3(int capacity) {}
}
```
- 클래스에 `<E>` 선언하면 → 인스턴스 멤버(생성자 포함)에서 사용 가능
- 생성자는 클래스 타입 파라미터 사용하기에 생략하고
- 제네릭 메서드는 메서드 자체의 타입 파라미터 사용해서 `<T>` 등 다시 쓴다.
- 생성자에서 `<E>`와 별개의 타입 파라미터가 필요하면 쓴다.
```java
//필요한 경우
public class HashSet3<E> {
    //생성자에만 쓰이는 별도의 제네릭 타입
    <T> HashSet3(T initialValue, int capacity) {
        //T는 이 생성자에서만 유효
        //E는 클래스 전체에서 유효
    }
}
```
```java
//다음 패턴을 더 많이 쓴다.
public class HashSet3<E> {
    //그냥 Object나 특정 타입으로 받기
    HashSet3(Object initialValue, int capacity) {
    }
    //또는 팩토리 메서드 사용
    public static <T, E> HashSet3<E> fromCollection(
            Collection<T> source,
            Function<T, E> converter,
            int capacity
    ) {
        HashSet3<E> set = new HashSet3<>(capacity);
        //변환 로직
        return set;
    }
}
```
- (2) 생성자 주입과 제네릭 타입은 다르다.
```java
//생성자 주입
public class Service {
    private Repository repo;
    Service(Repository repo) { //실제 객체를 받음
        this.repo = repo;
    }
}
```
```java
//제네릭은 컴파일 타임에 결정
HashSet3<String> set = new HashSet3<>(10); //여기서 E = String으로 결정됨
//E = String인 HashSet3의 생성자가 호출됨
//buckets는 LinkedList<String>[] 타입이 됨
//타입 소거로 인해 런타임에는 E 정보가 사라짐
```
- 객체를 생성할 때 타입을 지정하면, 컴파일러가 그 정보를 생성자에 전달
- 컴파일 시점에 타입 체크가 이루어져서 런타임에 안전하게 사용할 수 있다.
- (3) 제네릭 타입 캐스팅 시점
```java
HashSet3<String> set = new HashSet3<>(10);
//이렇게 생성할 때, 런타임에는 캐스팅이 일어나지 않는다.
//컴파일 타임: 컴파일러가 E = String이라고 타입 체크만 함
//컴파일 타임: 이 변수는 `LinkedList<String>[]`여야 해라고 검증
```
```java
//실제로는 이렇게 실행됨 (타입 소거 후)
buckets = new LinkedList[capacity]; //raw 타입 배열
//런타임에는 전부 이것과 동일:
LinkedList[] buckets = new LinkedList[10];
//하지만 컴파일러가 다음 코드를 막아줌:
HashSet3<String> set = new HashSet3<>(10);
set.add(123); //컴파일 에러
```
- 캐스팅은 값을 꺼낼 때 일어난다.
```java
HashSet3<String> set = new HashSet3<>(10);
set.add("hello");
//나중에 값을 꺼낼 때:
String value = set.get(...); //여기서 캐스팅
//작성한 코드:
String value = buckets[0].get(0);
//컴파일 후 실제 바이트코드:
String value = (String) buckets[0].get(0); //캐스팅 추가됨
```
```java
//실제로는 이렇게 저장됨:
LinkedList[] buckets; //<String> 정보 없음
buckets[0].add("hello"); //Object로 저장
//꺼낼 때:
Object obj = buckets[0].get(0); //Object 반환
String str = (String) obj; //캐스팅 필요
```
- 데이터 추가할 때: 캐스팅 없음 (Object로 자동 업캐스팅)
- 데이터 꺼낼 때: 컴파일러가 자동으로 다운캐스팅 코드 삽입
```java
public class HashSet3<E> {
    private LinkedList<E>[] buckets;
    public boolean contains(E element) {
        //내부에서 비교할 때
        for (E item : buckets[index]) { //컴파일러가 (E) 캐스팅 삽입
            if (item.equals(element)) {
                return true;
            }
        }
    }
}
```
- (4) ArrayList
- ArrayList는 내부에서 Object[]를 사용하고 타입 안전성을 관리
- ArrayList가 배열보다 약간 느릴 수는 있음
- 배열: `new LinkedList<E>[10]` 배열은 제네릭을 지원x
```java
LinkedList<String>[] arr = new LinkedList<String>[10]; //컴파일 에러
//배열은 런타임에 타입 정보를 가지고 있다. (Reifiable)
String[] strArr = new String[10];
Object[] objArr = strArr; //공변성: 업캐스팅 가능
//String이 Object의 자식이면
//String[]도 Object[]의 자식처럼 취급하는 게 공변성
objArr[0] = 123; //런타입 에러 발생
//배열은 모든 저장 시점에 런타임 타입 체크를 한다.
//if (!(123 instanceof String)) {
//    throw new ArrayStoreException();
//}
//하지만 제네릭은 제네릭은 타입 소거로 런타임에 정보가 사라짐
LinkedList[] arr = new LinkedList[10];
//instanceof 체크할 수 없으니 타입 안전성을 보장 못 함
//자바가 제네릭 배열 생성을 막음
```
- ArrayList: `new ArrayList<LinkedList<E>>()` ArrayList는 지원o
```java
ArrayList<String> list = new ArrayList<>();
//ArrayList는 불변성(invariant): 공변성이 없다.
//String이 Object의 자식이어도
//ArrayList<String>과 ArrayList<Object>는 다른 타입
ArrayList<Object> objList = list; //컴파일 에러
//만약 가능했다면:
//objList.add(123); //String 리스트에 Integer가 들어가는 위험
```
- 제네릭 배열이 불가능한 이유
```java
//만약 이게 가능하다면:
LinkedList<String>[] arr = new LinkedList<String>[10];
Object[] objArr = arr; //배열은 공변성이 있으니 가능
objArr[0] = new LinkedList<Integer>(); //런타임에는 타입 소거로 체크 불가
```
- 배열의 공변성은 제네릭 도입 전부터 있었던, 하위 호환성을 위한 설계
```java
//이런 걸 가능하게 하려고:
public static void printAll(Object[] arr) {
    for (Object obj : arr) {
        System.out.println(obj);
    }
}
String[] names = {"Alice", "Bob"};
printAll(names); //String[]을 Object[]로 전달 가능
//String[] -> Object[] 대입이 필수인 게 아니라
//그럴 상황에 대비한 거였다.
//제네릭 배열은 대입할 때 캐스팅 못하는 위험을 경고로 미리 막는다.
buckets = (LinkedList<E>[]) new LinkedList[capacity];
//raw 타입을 제네릭 타입으로 다운캐스팅: unchecked 경고
ArrayList<LinkedList<String>> typed = new ArrayList<>();
ArrayList<LinkedList> raw = typed;
//제네릭 타입을 raw 타입으로 업캐스팅: unchecked 경고
```
- 타입 소거로 인한 자동 업캐스팅
```java
//타입 소거로 자동으로 일어난다.
buckets[i] = new LinkedList<>(); //new LinkedList<E>()
//내부적으로:
//buckets는 런타임에 LinkedList[] (raw 타입)
//new LinkedList<E>()는 컴파일 후 LinkedList (raw)
//자동 업캐스팅 (타입 소거로 인해, 경고 없음)
```
- (5) 와일드카드
- 와일드카드는 타입 안전성을 보장하는 방식으로 설계됨
```java
//Raw 타입 - 읽기/쓰기 모두 가능, 타입 안전성 X
ArrayList<LinkedList<String>> typed = new ArrayList<>();
ArrayList<LinkedList> raw = typed; //경고
raw.add(new LinkedList<Integer>()); //컴파일됨: 위험
```
```java
//와일드카드
ArrayList<? extends LinkedList> wildcard = ...;
// 컴파일러 입장:
// "이게 ArrayList<LinkedList<String>>일까?
//  아니면 ArrayList<LinkedList<Integer>>일까?
//  아니면 ArrayList<MyLinkedList<Double>>일까?
//  모르겠으니까 아무것도 못 넣게 하자"
ArrayList<? extends Number> nums = ...;
//Number의 하위 타입이 많음:
//Integer, Double, Float, Long, ...
nums.add(123); //Integer일지 모름
nums.add(3.14); //Double일지 모름
//ArrayList<Double>이었다면:
ArrayList<Double> doubles = new ArrayList<>();
ArrayList<? extends Number> nums = doubles;
nums.add(123); //Integer를 Double 리스트에 추가x
```
- 와일드카드 종류
```java
//비제한 와일드카드 <?> - Object로 읽기 가능
ArrayList<?> unknown = new ArrayList<String>();
Object obj = unknown.get(0); //Object로만 읽기 가능
unknown.add("hello"); //불가
unknown.add(null); //null만 가능
//Upper Bounded <? extends T> - T로 읽기 가능
ArrayList<? extends Number> nums = new ArrayList<Integer>();
Number n = nums.get(0); //Number로 읽기 가능
nums.add(123); //불가
nums.add(null); //null만 가능
// "뭔지 모르지만 Number의 하위 타입이다."
//Lower Bounded <? super T> - Object로 읽기 가능, T와 T 하위 타입 쓰기 가능
ArrayList<? super Integer> ints = new ArrayList<Number>();
ints.add(123); //Integer 추가 가능
Object obj = ints.get(0); //Object로만 읽기 가능
Integer i = ints.get(0); //불가
```
- (7) ArrayList 적용
- 251007-java-mid2/src/hash/HashSet3.java
```java
public class HashSet3<E> {
    private ArrayList<LinkedList<E>> buckets;
    private int capacity;
    private int size;
    HashSet3(int capacity) {
        buckets = new ArrayList<>(capacity); //초기 용량 지정해서 생성
        for (int i = 0; i < capacity; i++) {
            buckets.add(new LinkedList<>());
        }
        this.capacity = capacity;
        this.size = 0;
    }
    //...
}
```
- 배열 대신 ArrayList 사용
- 배열 접근: `buckets[index]` (O(1) - 직접 접근)
- ArrayList 접근: `buckets.get(index)` (O(1) - 내부적으로 배열 사용)
- buckets - `ArrayList<LinkedList<E>>` 타입
- ArrayList의 메서드만 buckets.로 사용 가능
- (객체 이름) - `HashSet3<E>` 타입
- HashSet3의 메서드만 (객체 이름).으로 사용 가능
- 3. 문자열 해시 코드
- 해시 함수는 임의의 길이의 데이터를 입력으로 받아
- 고정된 길이의 해시값을 출력하는 함수를 말한다.
- 고정된 길이=저장 공간의 크기 (int형 4byte 등)
- 해시 충돌: 서로 다른 데이터 입력했는데 같은 해시 코드
- 문자열은 char 배열로 (int)로 바꿔서 계산을 하면 될 것 같다.
- 다른 건 어떻게 계산할까?
- 4. Object.hashCode()
```java
/*
//몇 번을 해도 같게 나옴
Bucket 3: [ㄱ, ㅏ]
Bucket 6: [.]
Bucket 7: [a]
Bucket 8: [b]
 */
```
- 보통 Object의 hashCode 메서드를 재정의해서 사용한다.
- Object는 객체의 참조값을 기반으로 해시 코드를 생성한다.
- 해시 코드의 경우 정수를 반환해서 마이너스 값이 나올 수 있다.
- Member라고 할 때, 같은 id 가진 객체는 equals하면 같아야 한다.
- Member라고 할 때, 같은 id 가진 객체는 같은 해시코드여야 한다.
```java
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Member member = (Member) obj;
        return Objects.equals(id, member.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
```
- id 중복 객체 생성 방지가 우선된다.
```java
//생성자에서 검증
public class Member {
    private static Set<String> usedIds = new HashSet<>();
    private String id;
    public Member(String id, String name) {
        if (usedIds.contains(id)) {
            throw new IllegalArgumentException("이미 사용 중인 ID입니다: " + id);
        }
        this.id = id;
        usedIds.add(id);
    }
}
//Factory 메서드 사용
public class MemberFactory {
    private Set<String> usedIds = new HashSet<>();
    public Member createMember(String id, String name) {
        if (usedIds.contains(id)) {
            throw new IllegalArgumentException("중복된 ID");
        }
        usedIds.add(id);
        return new Member(id, name);
    }
}
//컬렉션에서 관리
public class MemberRepository {
    private Map<String, Member> members = new HashMap<>();
    public void add(Member member) {
        if (members.containsKey(member.getId())) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다");
        }
        members.put(member.getId(), member);
    }
}
```
- 동일성 Identity: == 참조가 동일한 객체 가리키는지 확인
- 동등성 Equality: equals()로 두 객체가 논리적 동등한지 확인
- hashCode()가 같음 → equals()는 true일 수도, false일 수도 있다.
- equals()가 true → hashCode()는 반드시 같아야 한다.
```
//자바 HashSet.contains() 실행 시
1. hashCode() 호출 → 버킷 찾기
//hashCode를 hash(내부값)으로 재정의하지 않으면 버킷을 못 찾는다.
2. 같은 버킷 내에서 equals() 호출 → 실제 중복 (이미 저장돼 있는지) 확인
//equals를 내부값 비교로 재정의하지 않으면 버킷에 여러 데이터 있을 때 확인 못 한다.
```
- 기본 클래스도 hashCode, equals 내부값 기반으로 재정의했다.
- HashSet3.java: E 타입 객체가 오버라이드한 hashCode()가 실행된다.
```
┌─────────────────┐
│  Method Area    │ ← hashCode() 메서드의 바이트코드 저장
├─────────────────┤
│     Heap        │ ← Member 객체 인스턴스 저장 (member가 가리킴)
├─────────────────┤
│     Stack       │ ← member 변수(참조), index 변수
│  (Thread 별)    │   hashIndex() 호출 정보
└─────────────────┘
```
- 5. 제네릭과 인터페이스 도입
- 251007-java-mid2/src/set/HashSet4.java
- `buckets = new LinkedList[capacity];` 노란 줄은 뜨는데 경고가 안 나왔다.
- `set.add(1);` 컴파일 에러 났다. (컴파일 시 제네릭 타입 체크)