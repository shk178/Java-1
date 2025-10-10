# 11. 컬렉션 프레임워크 - 순회, 정렬
- 1. 직접 구현 Iterable, Iterator
- Iterable은 반복 가능한 객체(컬렉션 등)가 구현하는 인터페이스
- iterator() 메서드는
- 해당 객체의 요소를 순회할 수 있는 Iterator를 반환
```java
public interface Iterable<T> {
    Iterator<T> iterator(); //반드시 구현해야 함
    //default 메서드들 (JDK 8 이후 추가)
    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
    }
    default Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }
}
```
- Iterator는 요소를 하나씩 순회하기 위한 인터페이스
- hasNext() → 아직 읽지 않은 요소가 남아 있으면 true
- next() → 다음 요소를 반환하고 커서를 이동
- 즉, 가리키는 위치가 다음 요소로 바뀜
- 다음 요소가 없는데 next()를 호출하면 NoSuchElementException
- remove() → 마지막으로 반환된 요소를 삭제
```java
//ArrayList 내부에서 사용하는 내부 전용(iterator) 클래스
private class Itr implements Iterator<E> {
    int cursor;       //다음에 반환할 요소의 인덱스
    int lastRet = -1; //마지막으로 반환한 요소의 인덱스 (remove()용)
    int expectedModCount = modCount; //구조 변경 감지용 (fail-fast)
    public boolean hasNext() {
        return cursor != size;
    }
    public E next() {
        checkForComodification();
        int i = cursor;
        if (i >= size)
            throw new NoSuchElementException();
        Object[] elementData = ArrayList.this.elementData;
        cursor = i + 1;                //커서를 한 칸 앞으로 이동
        lastRet = i;                   //방금 반환한 위치 기록
        return (E) elementData[i];     //i번째 요소 반환
    }
    public void remove() {
        if (lastRet < 0)
            throw new IllegalStateException();
        checkForComodification();
        ArrayList.this.remove(lastRet);
        cursor = lastRet;              //삭제 후 인덱스 조정
        lastRet = -1;
        expectedModCount = modCount;
    }
    final void checkForComodification() {
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }
}
```
```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    public Iterator<E> iterator() {
        return new Itr(); //여기서 내부 클래스 Itr을 반환
    }
    private class Itr implements Iterator<E> {
        //내부 상태 관리용 필드들
        int cursor;
        int lastRet = -1;
        int expectedModCount = modCount;
        public boolean hasNext() { ... }
        public E next() { ... }
        public void remove() { ... }
    }
}
```
- 2. 향상된 for문
```java
public static void main(String[] args) {
    MyArr arr = new MyArr(new int[]{10, 20, 30});
    for (int n : arr) {
        System.out.println(n);
    }
}
//컴파일러가 실행
//ArrIterator 객체 생성, .hasNext() 호출, .next() 호출
for (Iterator<Integer> it = arr.iterator(); it.hasNext(); ) {
int n = it.next();
    System.out.println(n);
}
```
- 3. 자바 제공 Iterable, Iterator
```java
public interface Iterable<T> {
    Iterator<T> iterator();                     //(1)
    default void forEach(Consumer<? super T> action) //(2)
    default Spliterator<T> spliterator();       //(3)
}
```
```scss
Iterable
│
├── iterator() ──→ Iterator<T> ──→ hasNext(), next()
│        ↑
│        └─ (for-each, forEach()에서 사용)
│
└── spliterator() ──→ Spliterator<T> ──→ trySplit(), forEachRemaining()
         ↑
         └─ (Stream, parallelStream()에서 사용)
```
- (1) `Iterator<T> iterator()`
- 가장 기본적인 반복 도구
- 반환 타입: `Iterator<T>`
- 주 목적: for-each문, 수동 순회에 사용, 단일 스레드, 순차적 순회용
- 대표 메서드: boolean hasNext(), T next()
- (2) `default void forEach(Consumer<? super T> action)`
- 목적: 람다식으로 간단히 반복할 수 있게 함, 순차적 처리용 (병렬 X)
- 내부 구현은 단순히 iterator()를 (=(1)번) 사용
```java
list.forEach(x -> System.out.println(x));
//위 코드랑 아래 코드 같음
for (T x : list) {
    System.out.println(x);
}
```
- (3) `default Spliterator<T> spliterator()`
- 반환 타입: `Spliterator<T>` (split + iterator)
- 목적: 데이터를 쪼개서 병렬로 처리할 수 있게 함, Stream API가 사용
- 내부 Iterator와 유사하지만, trySplit() 메서드를 추가로 가짐
```java
list.stream() //내부적으로 list.spliterator() 호출
//컬렉션이 Spliterator를 생성해서 Stream이 그 데이터를 순회
```
- (4) Iterable 인터페이스 <- Collection 인터페이스
- Collection <- List/Queue/Set 인터페이스
- List <- ArrayList/LinkedList 클래스
- Queue <- Deque 인터페이스 <- LinkedList/ArrayDeque 클래스
- Set <- HashSet/TreeSet 클래스
- HashSet <- LinkedHashSet 클래스
- Map 인터페이스 <- HashMap/TreeMap 클래스
- HashMap <- LinkedHashMap 클래스
- Iterable 인터페이스로부터 내려간 클래스들에 맞는 Iterator 다 구현됨
- Map은 Key, Value가 다 있어서 바로 순회 불가
- Set = keySet(), Collection = values()으로 K 또는 V를 정해서 순회
- Set = entrySet()으로도 순회 가능
- 251007-java-mid2/src/iter1/IterMain.java
- printAll(Iterator<>), foreach(Iterable<>) 메서드 작성했는데
- Iterable, Iterator 인터페이스여서 다형성 적용됨
- 하위 구현체들은 코드 변경 없이 쓸 수 있다.
- class java.util.ArrayList$Itr
- ArrayList의 Iterator는 ArrayList의 내부 클래스다.
- class java.util.HashMap$KeyIterator
- HashSet은 내부에서 HashMap을 사용한다.
- class java.util.ArrayList (Iterable에 ArrayList 대입)
- class java.util.HashSet (Iterable에 HashSet 대입)
- 4. 정렬 - Comparable, Comparator 1
- (1) Comparable — 자기 자신이 비교 기준을 가진다.
- Comparable 인터페이스는 객체 자신이 정렬 기준을 정의할 때 사용
- 클래스 내부에 나는 이렇게 정렬돼야 해라는 규칙을 내장
```java
public class Person implements Comparable<Person> {
    private String name;
    private int age;
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    //compareTo() 오버라이드
    @Override
    public int compareTo(Person other) {
        //나이를 기준으로 오름차순 정렬
        return this.age - other.age;
    }
    @Override
    public String toString() {
        return name + "(" + age + ")";
    }
}
//Person은 스스로 비교 기준을 가지고 있기 때문에
//Collections.sort()나 Arrays.sort()에 그냥 넘겨도 된다.
List<Person> people = List.of(
    new Person("Alice", 25),
    new Person("Bob", 20),
    new Person("Charlie", 30)
);
List<Person> sorted = new ArrayList<>(people);
Collections.sort(sorted);
System.out.println(sorted); //[Bob(20), Alice(25), Charlie(30)]
```
- `Comparable<T>` → 클래스 내부에서 비교 기준을 정의
- 반드시 `compareTo(T o)` 메서드 구현
- 1개의 기본 정렬 기준만 가능
- 자연 순서(Natural Order)를 정함
- 예: String, Integer 등은 이미 Comparable 구현되어 있음
- (2) Comparator — 외부에서 비교 기준을 만든다
- Comparator는 클래스 외부에서 정렬 기준을 따로 정의할 때 사용
- 이 객체를 이번에는 이렇게 정렬하고 싶다라고 상황별로 다르게 정렬 가능
```java
import java.util.Comparator;
public class NameComparator implements Comparator<Person> {
    @Override
    public int compare(Person p1, Person p2) {
        return p1.name.compareTo(p2.name); //이름 기준으로 오름차순
    }
}
List<Person> people = new ArrayList<>(List.of(
    new Person("Charlie", 30),
    new Person("Alice", 25),
    new Person("Bob", 20)
));
Collections.sort(people, new NameComparator());
System.out.println(people); //[Alice(25), Bob(20), Charlie(30)]
//익명 클래스 / 람다식으로 간단히 작성할 수도 있다.
```
- `Comparator<T>` → 클래스 외부에서 비교 기준 정의
- compare(T o1, T o2) 메서드 구현
- 여러 정렬 기준을 만들 수 있음 (상황별 정렬 가능)
- 기존 클래스 수정 없이도 정렬 가능
- Comparable, Comparator 둘 다 써도 된다.
```java
Collections.sort(people); //기본적으로 나이순 정렬
Collections.sort(people, new NameComparator()); //필요할 때 이름순 정렬
```
- (3) 정렬 알고리즘
```
초기 Java (과거) - Quicksort
- 단일 피벗 기반의 퀵 정렬 사용
기본형 배열 (int[], double[] 등) - Dual-Pivot Quicksort
- 두 개의 피벗을 사용하여 성능 향상, Arrays.sort(int[]) 등에서 사용됨
객체 배열 (String[], Custom Object[]) - TimSort
- 병합 정렬 + 삽입 정렬 기반의 안정 정렬
- Arrays.sort(Object[]) 및 `Collections.sort()에서 사용됨
```
- 퀵소트 불안정, O(nlogn), 빠르지만 최악이 O(n^2)
- 듀얼피벗 불안정, O(nlogn), 두 피벗으로 성능 개선, 기본형 최적화
- 기본형 배열은 값 자체를 비교하므로 성능이 중요
- 팀소트 안정적, O(nlogn), 실생활 데이터에 강함, 객체 정렬 적합
- 객체 배열은 안정성이 중요 (예: 동일한 키 값의 순서 유지)
- 5. 정렬 - Comparable, Comparator 2