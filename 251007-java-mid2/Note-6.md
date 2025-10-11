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
- (1) 자바 컬렉션 프레임워크
```
java.lang.Object
    ↓
java.lang.Iterable<E> (인터페이스)
        public interface Iterable<E> {
            Iterator<E> iterator();
            //forEach, spliterator 등
        }
        - 반복 가능한 객체의 최상위 인터페이스
        - for-each 문 사용 가능하게 함
        - 모든 Collection은 Iterable을 상속
        - iterator(): Iterator 객체 반환
        - forEach(Consumer): 각 요소에 대해 작업 수행
    ↓
java.util.Collection<E> (인터페이스)
        public interface Collection<E> extends Iterable<E> {
            boolean add(E e);
            boolean remove(Object o);
            int size();
            boolean isEmpty();
            boolean contains(Object o);
            // 등등...
        }
        - 모든 컬렉션의 최상위 인터페이스
        - List, Set, Queue의 공통 기능 정의
        - add(), remove(), clear(), size(), isEmpty()
        - contains(), containsAll(), toArray()
        - 직접 정렬 불가 (Collection은 추상적)
        - 구현체(List 등)에서 정렬 가능
    ↓
    ├─ List<E> (인터페이스)
            public interface List<E> extends Collection<E> {
                E get(int index);
                E set(int index, E element);
                void add(int index, E element);
                //등등...
            }
            - 순서가 있는 컬렉션
            - 중복 허용
            - 인덱스로 접근 가능
    ├─ Set<E> (인터페이스)
            public interface Set<E> extends Collection<E> {
                //Collection과 동일한 메서드
                //중복을 허용하지 않는다는 의미론적 차이
            }
            - 중복 불허
            - 순서 보장 안 함 (구현체에 따라 다름)
    └─ Queue<E> (인터페이스)
            public interface Queue<E> extends Collection<E> {
                boolean offer(E e);
                E poll();
                E peek();
            }
            - FIFO (First-In-First-Out) 구조
            - 대기열, 버퍼링에 사용
별도:
java.util.Map<K,V> (인터페이스) - Collection 미상속
    public interface Map<K,V> {
        V put(K key, V value);
        V get(Object key);
        V remove(Object key);
        Set<K> keySet();
        Collection<V> values();
        Set<Map.Entry<K,V>> entrySet();
    }
    - Key-Value 쌍 저장
    - Collection 인터페이스 상속 안 함 (별도 계층)
    - Key는 중복 불가, Value는 중복 가능
```
- (2) List 구현체와 정렬
```java
/* ArrayList */
List<Integer> list = new ArrayList<>();
Collections.sort(list);
//정렬 알고리즘: 내부 배열을 TimSort로 정렬, O(nlogn)
//내부 구조: 동적 배열
//인덱스 접근 O(1),중간 삽입/삭제 O(n), 크기 조정 시 배열 복사
/* LinkedList */
List<String> list = new LinkedList<>();
Collections.sort(list);
//정렬 알고리즘: TimSort (배열로 변환 후), O(nlogn) + 변환 비용
//내부 구조: 이중 연결 리스트
//순차 접근 O(n), 양 끝 삽입/삭제 O(1), 중간 삽입/삭제 O(1) (위치만 안다면)
/* Vector (레거시) */
Vector<Integer> vector = new Vector<>();
Collections.sort(vector);
//정렬 알고리즘: TimSort, O(nlogn)
//동기화된 ArrayList (스레드 안전)
//현대: ArrayList + Collections.synchronizedList() 권장
/* Stack (레거시) */
Stack<Integer> stack = new Stack<>();
List<Integer> list = new ArrayList<>(stack);
Collections.sort(list);
//정렬은 List 변환 후 TimSort
//정렬 없음 (LIFO 구조), Vector를 상속
//현대: ArrayDeque 권장
```
- (3) Set 구현체와 정렬
```java
/* HashSet */
Set<Integer> set = new HashSet<>();
//정렬 불가 - 순서 없음
//List로 변환 후 정렬 필요
//O(n) 변환 + O(nlogn) 정렬
List<Integer> list = new ArrayList<>(set);
Collections.sort(list);
//내부 구조: HashMap 기반
//추가/삭제/검색 O(1)
//가장 빠른 Set
/* LinkedHashSet */
Set<String> set = new LinkedHashSet<>();
List<String> list = new ArrayList<>(set);
Collections.sort(list);
//삽입 순서 유지
//정렬은 List 변환 후 별도 필요
//내부 구조: LinkedHashMap 기반
/* TreeSet */
TreeSet<Integer> treeSet = new TreeSet<>();
//자동으로 정렬된 상태 유지
//정렬 알고리즘: Red-Black Tree, 삽입/삭제/검색 O(logn)
//first(), last(), higher(), lower() 등 메서드 제공
/* SortedSet 인터페이스 */
public interface SortedSet<E> extends Set<E> {
    Comparator<? super E> comparator();
    E first();
    E last();
    SortedSet<E> subSet(E fromElement, E toElement);
}
//TreeSet이 구현하는 인터페이스
//정렬된 Set을 위한 추가 메서드 제공
/* NavigableSet 인터페이스 */
public interface NavigableSet<E> extends SortedSet<E> {
    E lower(E e);
    E floor(E e);
    E ceiling(E e);
    E higher(E e);
}
//TreeSet이 구현
//탐색 관련 고급 메서드 제공
```
- (4) Queue 구현체와 정렬
```java
/* PriorityQueue */
PriorityQueue<Integer> pq = new PriorityQueue<>();
//자동으로 최소 힙 유지 (정렬 아님)
//알고리즘: Binary Heap (이진 힙)
//삽입/삭제 O(logn), 최소값 조회 O(1)
//최솟값이 항상 루트에 위치
//완전 정렬은 아님 (부분 정렬)
//poll() 시 최솟값부터 반환
//Comparator로 정렬 기준(최소/최대) 변경 가능
/* Deque 인터페이스 */
public interface Deque<E> extends Queue<E> {
    void addFirst(E e);
    void addLast(E e);
    E removeFirst();
    E removeLast();
}
//양방향 큐 (Double Ended Queue)
//Stack과 Queue 기능 모두 제공
/* ArrayDeque */
Deque<Integer> deque = new ArrayDeque<>();
//정렬 불가 - List 변환 후 정렬
//정렬: List 변환 후 가능
//내부 구조: 순환 배열
//Stack, Queue보다 빠름
//null 불허
//양 끝 삽입/삭제 O(1)
/* LinkedList (Queue/Deque로 사용) */
Queue<Integer> queue = new LinkedList<>();
Deque<Integer> deque = new LinkedList<>();
//List, Queue, Deque 모두 구현
//정렬은 List로 취급 시 가능
```
- (5) Map 구현체와 정렬
```java
/* HashMap */
Map<String, Integer> map = new HashMap<>();
//알고리즘: TimSort (변환 후)
//Key 정렬: keySet()을 List로 변환 후 정렬
List<String> keys = new ArrayList<>(map.keySet());
Collections.sort(keys);
//Value 정렬: entrySet()을 List로 변환 후 Comparator 사용
List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
entries.sort(Map.Entry.comparingByValue());
//내부 구조: Hash Table
//O(1), 가장 빠른 Map
/* LinkedHashMap */
Map<String, Integer> map = new LinkedHashMap<>();
//삽입 순서 유지
//정렬: HashMap과 동일 방법
//내부 구조: HashMap + 연결 리스트
/* TreeMap */
TreeMap<String, Integer> treeMap = new TreeMap<>();
//Key 기준 자동 정렬
//Value 정렬 별도로 해야 함
//정렬 알고리즘: Red-Black Tree, O(logn)
//firstKey(), lastKey(), higherKey() 등 메서드
/* Hashtable (레거시) */
Hashtable<String, Integer> table = new Hashtable<>();
//정렬: HashMap과 동일 방법
//동기화된 HashMap
//현대: ConcurrentHashMap 권장
/* SortedMap 인터페이스 */
public interface SortedMap<K,V> extends Map<K,V> {
    Comparator<? super K> comparator();
    K firstKey();
    K lastKey();
}
//TreeMap이 구현하는 인터페이스
/* NavigableMap 인터페이스 */
public interface NavigableMap<K,V> extends SortedMap<K,V> {
    Map.Entry<K,V> lowerEntry(K key);
    K lowerKey(K key);
    Map.Entry<K,V> higherEntry(K key);
    K higherKey(K key);
}
//TreeMap이 구현
//탐색 관련 고급 메서드 제공
```
- (6) 정렬 방법, 알고리즘, 시간 복잡도, 안정성
```
Iterable<E>
    │
    └─ Collection<E>
           │
           ├─ List<E>
           │    ├─ ArrayList: Collections.sort(), TimSort, O(nlogn), 안정
           │    ├─ LinkedList: Collections.sort(), TimSort, O(nlogn), 안정
           │    ├─ Vector (레거시): Collections.sort(), TimSort, O(nlogn), 안정
           │    └─ Stack (레거시, Vector 상속)
           │
           ├─ Set<E>
           │    ├─ HashSet: List 변환 후, TimSort, O(nlogn), 안정
           │    ├─ LinkedHashSet: List 변환 후, TimSort, O(nlogn), 안정
           │    └─ SortedSet<E>
           │         └─ NavigableSet<E>
           │              └─ TreeSet: 자동 정렬, Red-Black Tree, O(logn), 자동
           │
           └─ Queue<E>
                ├─ PriorityQueue: 자동(부분), Binary Heap, O(logn), 부분
                └─ Deque<E>
                     ├─ ArrayDeque: List 변환 후, TimSort, O(nlogn), 안정
                     └─ LinkedList
별도 계층:
Map<K,V>
    ├─ HashMap: 변환 후, TimSort, O(nlogn), 안정
    ├─ LinkedHashMap: 변환 후, TimSort, O(nlogn), 안정
    ├─ Hashtable (레거시)
    └─ SortedMap<K,V>
         └─ NavigableMap<K,V>
              └─ TreeMap: 자동(Key), Red-Black Tree, O(logn), 자동
선택 가이드:
Collection / Map
- 데이터 하나씩: Collection 계열
- Key-Value 쌍: Map 계열
List / Set / Queue
- 순서 + 중복 허용: List
- 중복 불허: Set
- FIFO/우선순위: Queue
자동 정렬 필요
- TreeSet: 중복 없는 정렬된 데이터
- TreeMap: Key 기준 정렬된 맵
- PriorityQueue: 최소/최대값 우선 처리
수동 정렬 필요
- Comparator 구현: 커스텀 정렬
- ArrayList + Collections.sort(): 가장 일반적
- 배열 + Arrays.sort(): 기본 타입에 효율적
성능 우선
- ArrayList: 일반적인 리스트
- HashSet: 빠른 중복 제거
- HashMap: 빠른 검색
- ArrayDeque: 양방향 큐
```
- (6) 배열은 컬렉션 프레임워크 밖
```
//컬렉션 프레임워크는 Java 1.2부터 도입 (1998년)
컬렉션 프레임워크 (Collection Framework)
├─ Iterable → Collection → List, Set, Queue
└─ Map
//배열은 Java 1.0부터 존재 (1996년)
배열 (Array) - 별도의 독립적인 자료구조
├─ int[], double[], char[] 등 (기본 타입 배열)
└─ String[], Object[] 등 (객체 배열)
```
```java
//배열은 Iterable, Collection 인터페이스를 구현하지 않음
int[] arr = {1, 2, 3};
//arr.add() //불가
//arr.remove() //불가
//arr.size() //불가 (arr.length만 가능)
//for-each는 가능 (컴파일러의 특별 처리)
for (int num : arr) { //가능하지만 Iterable 때문은 아님
    System.out.println(num);
}
```
```java
//배열은 크기 변경 불가
int[] arr = new int[5]; //크기 고정
//arr의 크기를 바꿀 수 없음
//컬렉션은 크기 동적 변경 가능
List<Integer> list = new ArrayList<>();
list.add(1); //크기 자동 증가
list.remove(0); //크기 자동 감소
```
```java
//배열은 기본 타입 직접 저장 가능
int[] arr = {1, 2, 3}; //가능
//컬렉션은 객체만 저장 (제네릭 사용)
//List<int> list; //불가
List<Integer> list; //가능 (오토박싱 필요)
```
- Arrays.sort() - 배열 전용
```java
//기본 타입 배열
int[] arr = {5, 2, 8, 1};
Arrays.sort(arr); //Dual-Pivot Quicksort
//객체 배열
String[] names = {"John", "Alice", "Bob"};
Arrays.sort(names); //TimSort
//대상: 배열 (int[], Object[] 등)
//위치: java.util.Arrays 유틸리티 클래스
//이유: 배열은 Collection이 아니므로 별도 메서드 필요
```
- Collections.sort() - 컬렉션 전용
```java
List<Integer> list = new ArrayList<>(Arrays.asList(5, 2, 8, 1));
Collections.sort(list); //TimSort
//대상: List 인터페이스 구현체
//위치: java.util.Collections 유틸리티 클래스
//이유: Collection 계열 자료구조 정렬
```
- 배열 ↔ 컬렉션 변환: 배열 → 리스트
```java
//방법 1: Arrays.asList() (고정 크기)
String[] arr = {"A", "B", "C"};
List<String> list1 = Arrays.asList(arr);
//list1.add("D"); //UnsupportedOperationException
//방법 2: 새 ArrayList 생성 (가변 크기)
List<String> list2 = new ArrayList<>(Arrays.asList(arr));
list2.add("D"); //가능
//방법 3: List.of() (불변)
List<String> list3 = List.of(arr);
//list3.add("D"); //UnsupportedOperationException
```
- 배열 ↔ 컬렉션 변환: 리스트 → 배열
```java
List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
//방법 1: toArray(T[] array)
String[] arr1 = list.toArray(new String[0]);
//방법 2: 크기 지정
String[] arr2 = list.toArray(new String[list.size()]);
//기본 타입은 스트림 사용
List<Integer> numbers = Arrays.asList(1, 2, 3);
int[] intArr = numbers.stream().mapToInt(Integer::intValue).toArray();
```
- 배열 / 컬렉션
```
- 배열은 인터페이스 없음, 컬렉션은 Iterable, Collection 등
- 배열은 고정 크기, 컬렉션은 동적 크기
- 배열은 기본 타입 직접 저장, 컬렉션은 래퍼 클래스 필요
- 배열은 정렬 메서드 Arrays.sort(), 컬렉션은 Collections.sort()
- 배열은 length 메서드만, 컬렉션은 메서드 다양
- 배열은 성능 빠름, 컬렉션은 오브젝트 생성으로 약간 느림
- 타입 안정성 배열은 컴파일 타임 체크, 컬렉션은 제네릭으로 체크
```
- 배열 사용하는 경우
```java
//1. 크기가 고정되어 있을 때
int[] scores = new int[5];
//2. 기본 타입으로 대량 데이터 처리 (성능 중요)
int[] largeData = new int[1_000_000];
//3. 다차원 구조가 필요할 때
int[][] matrix = new int[10][10];
//4. 메서드 시그니처가 배열을 요구할 때
public static void main(String[] args) { } //args는 String[]
```
- 컬렉션 사용하는 경우
```java
//1. 크기가 변동될 때
List<String> names = new ArrayList<>();
names.add("New Name");
//2. 편리한 메서드가 필요할 때
list.contains("A");
list.indexOf("B");
list.removeAll(otherList);
//3. 제네릭 타입 안전성이 필요할 때
List<Integer> numbers = new ArrayList<>();
//4. 자동 정렬/중복 제거가 필요할 때
Set<String> uniqueNames = new TreeSet<>(); //자동 정렬 + 중복 제거
```
- 배열 정렬 알고리즘
```
int[]: Arrays.sort()
- Dual-Pivot Quicksort, O(nlogn), 불안정
Object[]: Arrays.sort()
- TimSort, O(nlogn), 안정
```
```java
/* 기본 타입 배열 */
int[] arr = {5, 2, 8, 1};
Arrays.sort(arr);
//알고리즘: Dual-Pivot Quicksort
//시간복잡도: 평균 O(nlogn), 최악 O(n^2)
//안정성: 불안정 정렬
//특징: 빠른 성능, 추가 메모리 적음
/* 객체 배열 */
String[] arr = {"John", "Alice", "Bob"};
Arrays.sort(arr);
//알고리즘: TimSort
//시간복잡도: O(nlogn)
//안정성: 안정 정렬
//특징: 부분 정렬된 데이터에 최적화
```
- (7) 힙 구조
- 완전 이진 트리(Complete Binary Tree) 기반의 자료구조
- 부모-자식 간에 특정 순서 관계를 유지
```
힙(Heap) = 완전 이진 트리 + 부모-자식 순서 관계
최소 힙: 부모 ≤ 자식 (루트 = 최솟값)
최대 힙: 부모 ≥ 자식 (루트 = 최댓값)
삽입: 마지막에 추가 → 상향 조정 O(logn)
삭제: 루트 제거 → 마지막을 루트로 → 하향 조정 O(logn)
조회: 루트 반환 O(1)
Java: PriorityQueue로 구현
```
- 최소 힙 (Min Heap)
```
        1           ← 루트가 최솟값
       / \
      2   3
     / \ / \
    4  5 6  7
- 부모 노드 ≤ 자식 노드 관계 유지
- Java의 PriorityQueue 기본값
```
- 최대 힙 (Max Heap)
```
        7           ← 루트가 최댓값
       / \
      6   5
     / \ / \
    4  3 2  1
- 부모 노드 ≥ 자식 노드 관계 유지
- PriorityQueue에 Collections.reverseOrder() 사용
```
- 힙은 완전 이진 트리다.
```declarative
완전 이진 트리            완전 이진 트리 아님
      1                        1
     / \                      / \
    2   3                    2   3
   / \                          / \
  4   5                        4   5
- 마지막 레벨을 제외한 모든 레벨이 꽉 참
- 마지막 레벨은 왼쪽부터 채워짐
```
- 힙은 배열로 구현된다.
```
- 배열로 트리 구현: 노드의 위치를 인덱스로 계산할 수 있어야 한다.
- 완전 이진 트리나 정적(구조가 고정된) 트리는 배열로 구현된다.
- 힙은 완전 이진 트리여서 배열로 구현된다.
- 일반 이진 탐색 트리, 편향 트리는 배열은 공간 낭비된다.
- 배열 인덱스가 많이 비어 있으면 부모-자식 간 인덱스 계산이
- 복잡해지거나 불가해져서 포인터 기반 연결 리스트로 구현된다.
/* 배열 기반 Prioirty Queue 구현 방식 */
- Priority Queue는 각 요소에 우선순위(priority)를 부여
- 우선순위가 높은 요소부터 먼저 처리하는 자료구조
1. 정렬된 배열 방식
- 요소를 우선순위에 따라 정렬된 상태로 저장합니다.
- 삽입 시: 새로운 요소를 올바른 위치에 삽입 (시간 복잡도 O(n))
- 삭제 시: 가장 앞 또는 뒤의 요소를 제거 (시간 복잡도 O(1))
2. 비정렬 배열 방식
- 요소를 그냥 배열에 추가하고, 삭제 시 우선순위가 가장 높은 요소를 찾아 제거
- 삽입 시: O(1)
- 삭제 시: O(n)
3. 배열 기반 힙 방식 (가장 일반적)
- 이진 힙(Binary Heap)을 배열로 구현하여 Priority Queue를 구성
- 삽입과 삭제 모두 O(logn)의 시간 복잡도
- 자식/부모 인덱스 계산으로 효율적인 구조 유지
힙 트리:
        1
       / \
      2   3
     / \ / \
    4  5 6  7
배열 표현:
인덱스: [0] [1] [2] [3] [4] [5] [6]
값:     [1] [2] [3] [4] [5] [6] [7]
인덱스 관계 (인덱스 0부터 시작):
- 부모 인덱스: (i-1) / 2
- 왼쪽 자식: 2*i + 1
- 오른쪽 자식: 2*i + 2
인덱스 관계 (인덱스 1부터 시작):
- 부모 인덱스: i / 2
- 왼쪽 자식: 2*i
- 오른쪽 자식: 2*i + 1
4. 어떤 방식이 좋을까
- 간단한 구현이 필요하면 정렬된 배열 방식
- 성능이 중요하다면 힙 기반 배열 방식이 가장 효율적
- Java의 PriorityQueue는 배열 기반 힙으로 구현됨
```
- 힙의 주요 연산 1. 삽입 (Insert) - O(logn)
```
예시: 최소 힙에 0 삽입
1단계: 마지막에 추가
        1
       / \
      2   3
     / \ / \
    4  5 6  7
   /
  0              ← 마지막 위치에 추가
2단계: 상향 조정 (Heapify Up / Bubble Up)
  0과 부모(4) 비교 → 교체
        1
       / \
      2   3
     / \ / \
    0  5 6  7
   /
  4
  0과 부모(2) 비교 → 교체
        1
       / \
      0   3
     / \ / \
    2  5 6  7
   /
  4
  0과 부모(1) 비교 → 교체
        0           ← 최종 위치
       / \
      1   3
     / \ / \
    2  5 6  7
   /
  4
```
```java
void insert(int value) {
    heap.add(value); //마지막에 추가
    int index = heap.size() - 1;
    //상향 조정
    while (index > 0) {
        int parent = (index - 1) / 2;
        if (heap.get(index) >= heap.get(parent)) break;
        //교체
        swap(index, parent);
        index = parent;
    }
}
```
- 힙의 주요 연산 2. 삭제 (Delete/Poll) - O(logn)
```
항상 루트(최솟값/최댓값)만 삭제
예시: 최소 힙에서 루트 삭제
1단계: 루트를 마지막 원소로 교체
        1              7
       / \            / \
      2   3    →     2   3
     / \ / \        / \ /
    4  5 6  7      4  5 6
2단계: 하향 조정 (Heapify Down / Bubble Down)
  7과 자식들(2,3) 비교 → 2와 교체
        2
       / \
      7   3
     / \ /
    4  5 6
  7과 자식들(4,5) 비교 → 4와 교체
        2
       / \
      4   3
     / \ /
    7  5 6       ← 최종 상태
```
```java
int poll() {
    int root = heap.get(0);
    int last = heap.remove(heap.size() - 1);
    if (heap.isEmpty()) return root;
    heap.set(0, last); //마지막 원소를 루트로
    int index = 0;
    //하향 조정
    while (true) {
        int left = 2 * index + 1;
        int right = 2 * index + 2;
        int smallest = index;
        if (left < heap.size() && heap.get(left) < heap.get(smallest))
            smallest = left;
        if (right < heap.size() && heap.get(right) < heap.get(smallest))
            smallest = right;
        if (smallest == index) break;
        swap(index, smallest);
        index = smallest;
    }
    return root;
}
```
- 힙의 주요 연산 3. 최솟값/최댓값 조회 (peek) - O(1)
```java
int peek() {
    return heap.get(0); //루트만 반환
}
```
- 자바의 PriorityQueue - 최대 힙으로 사용
```java
//방법 1: Collections.reverseOrder()
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
//방법 2: Comparator.reverseOrder()
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
//방법 3: 람다
PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);
maxHeap.offer(5);
maxHeap.offer(3);
maxHeap.offer(7);
System.out.println(maxHeap.poll()); //7 (최댓값)
```
- 자바의 PriorityQueue - 커스텀 객체 정렬
```java
class Task {
    String name;
    int priority;
    Task(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }
}
//우선순위가 낮은 것부터 (최소 힙)
PriorityQueue<Task> pq = new PriorityQueue<>(
    (a, b) -> a.priority - b.priority
);
pq.offer(new Task("Task1", 5));
pq.offer(new Task("Task2", 1));
pq.offer(new Task("Task3", 3));
System.out.println(pq.poll().name); //Task2 (priority=1)
```
- 자바의 PriorityQueue - 시간복잡도
```
연산 - 시간복잡도 - 설명
삽입 (offer) - O(logn) - 상향 조정 필요
삭제 (poll) - O(logn) - 하향 조정 필요
최솟값/최댓값 조회 (peek) - O(1) - 루트만 확인
특정 값 검색 - O(n) - 순서 보장 안 됨
힙 생성 - O(n) - Heapify 사용 시
```
- 자바 힙과 정렬된 배열, 이진 탐색 트리 - 시간복잡도
```
연산 - 힙 - 정렬된 배열 - 이진 탐색 트리
최솟값 찾기 - O(1) - O(1) - O(logn)~O(n)
최댓값 찾기 - O(1) - O(1) - O(logn)~O(n)
삽입 - O(logn) - O(n) - O(logn)~O(n)
삭제 - O(logn) - O(n) - O(logn)~O(n)
검색 - O(n) - O(logn) - O(logn)~O(n)
정렬 - 부분 정렬 - 완전 정렬 - 완전 정렬
메모리 - 효율적 - 효율적 - 포인터 필요
```
- (8) 자바 정렬 알고리즘
```
1. Arrays.sort()의 내부 알고리즘
기본 타입 배열 (int, double 등)
- Dual-Pivot Quicksort 사용
- Java 7부터 적용된 알고리즘
- 평균 시간복잡도: O(nlogn)
- 최악의 경우: O(n^2) (하지만 매우 드뭄)
객체 배열
- TimSort 사용
- 병합 정렬과 삽입 정렬의 하이브리드
- 안정 정렬(Stable Sort) - 동일한 값의 순서 유지
- 시간복잡도: O(nlogn)
2. Collections.sort()의 내부 알고리즘
- TimSort 사용
- List 컬렉션 정렬에 사용
- 안정 정렬
3. 주요 정렬 알고리즘 종류
버블 정렬 (Bubble Sort)
- 인접한 두 원소를 비교하며 정렬
- 시간복잡도: O(n^2)
- 학습용으로 적합, 실무에서는 비효율적
선택 정렬 (Selection Sort)
- 최솟값을 찾아 앞으로 이동
- 시간복잡도: O(n^2)
삽입 정렬 (Insertion Sort)
- 적절한 위치에 원소를 삽입
- 시간복잡도: O(n^2)
- 작은 데이터셋에서 효율적
병합 정렬 (Merge Sort)
- 분할 정복 방식
- 시간복잡도: O(nlogn)
- 안정 정렬
퀵 정렬 (Quick Sort)
- 피벗을 기준으로 분할
- 평균 시간복잡도: O(nlogn)
- 최악: O(n^2)
- 불안정 정렬
힙 정렬 (Heap Sort)
- 힙 자료구조 이용
- 시간복잡도: O(nlogn)
```
```java
//기본 타입 배열 정렬
int[] arr = {5, 2, 8, 1, 9};
Arrays.sort(arr);
//객체 배열 정렬 (Comparable)
String[] names = {"John", "Alice", "Bob"};
Arrays.sort(names);
//Collections 정렬
List<Integer> list = new ArrayList<>(Arrays.asList(5, 2, 8, 1, 9));
Collections.sort(list);
//Comparator 사용
Arrays.sort(arr, Collections.reverseOrder()); //내림차순
//Arrays.sort()나 Collections.sort() 사용 권장
//커스텀 정렬이 필요하면 Comparator 구현
//작은 데이터셋(<50개)은 삽입 정렬도 효율적
//안정 정렬이 필요하면 객체 배열이나 Collections 사용
```