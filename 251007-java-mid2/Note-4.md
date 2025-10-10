# 9. 컬렉션 프레임워크 - Set
```
Iterable
   ↑
Collection
   ↑
  ├── List
  │     ├── ArrayList
  │     ├── LinkedList
  │     └── Vector / Stack
  │
  ├── Set (중복을 허용하지 않음)
  │     ├── HashSet <- LinkedHashSet
  │     └── TreeSet
  │
  └── Queue
        ├── PriorityQueue
        └── LinkedList
```
- 1. LinkedHashSet
- HashSet + LinkedList 구조
- 대부분의 연산이 O(1) (hash 기반)
- null 1개 저장 가능
- 해시 기반으로 빠른 검색을 지원하면서도 입력 순서(order of insertion)를 기억
```java
public class HashSet<E> implements Set<E> {
    private transient HashMap<E,Object> map; //HashMap
    ...
}
public class LinkedHashSet<E> extends HashSet<E> {
    public LinkedHashSet() {
        super(new LinkedHashMap<E,Object>()); //LinkedHashMap
    }
}
//LinkedHashMap의 Entry(노드)
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before, after; //이중 연결 리스트
}
//요소가 추가될 때마다:
//해시 버킷에 저장 (HashMap처럼)
//동시에 이전/다음 노드와 연결 (LinkedList처럼)
//head <-> node1 <-> node2 <-> node3 <-> ... <-> tail
```
- 사용 코드
```java
import java.util.*;
public class LinkedHashSetDemo {
    public static void main(String[] args) {
        Set<String> set = new LinkedHashSet<>();
        set.add("A");
        set.add("B");
        set.add("C");
        for (String s : set) {
            System.out.print(s + " ");
        } //A B C
    }
}
```
- 2. TreeSet
- Red-Black Tree(균형 이진 탐색 트리) 구조
- 정렬된 순서로 요소를 저장
- 요소를 추가할 때마다 자동으로 정렬
- 기본적으로 오름차순(natural ordering)으로 정렬
- Comparator를 전달하면 사용자 정의 정렬도 가능
- 삽입·삭제·탐색 O(log n) (=균형 잡힌 트리 높이)
- 모든 원소에 대해 연산할 때 O(n log n)
- Comparable 인터페이스를 구현한 타입만 저장할 수 있다.
- 즉, 정렬 기준이 있어야 한다.
- 내부적으로 요소들을 비교(compareTo() 등)하므로
- null 값은 비교가 불가능해서 NullPointerException이 발생
- 사용 코드
```java
import java.util.*;
public class TreeSetExample {
    public static void main(String[] args) {
        //TreeSet 생성 (기본 정렬: 알파벳순=문자열 오름차순)
        Set<String> treeSet = new TreeSet<>();
        treeSet.add("Banana"); //add() 시 자동 정렬
        treeSet.add("Apple");
        treeSet.add("Cherry");
        System.out.println(treeSet); //[Apple, Banana, Cherry]
    }
}
```
```java
public class TreeSetCustomComparator {
    public static void main(String[] args) {
        //문자열 길이 기준으로 정렬하는 TreeSet
        //길이가 같으면 기본 문자열 순서(알파벳순)로 정렬
        Set<String> set = new TreeSet<>(Comparator.comparingInt(String::length));
        set.add("Banana");
        set.add("Apple");
        set.add("Kiwi");
        set.add("Watermelon");
        System.out.println(set); //[Kiwi, Apple, Banana, Watermelon]
    }
}
```
- 3. TreeSet 구조
```java
TreeSet<E> = TreeMap<E, Object>
//트리를 직접 구현한 클래스가 아니라, TreeMap을 사용해서 동작
//요소는 TreeMap의 key로 저장되고
//값(value)은 의미 없는 dummy object로 채워진다.
```
```java
public class TreeSet<E> extends AbstractSet<E>
        implements NavigableSet<E>, Cloneable, java.io.Serializable {
    //핵심: 내부적으로 TreeMap을 사용함
    private transient NavigableMap<E, Object> m;
    //더미 값 (TreeMap의 value용)
    private static final Object PRESENT = new Object();
    //기본 생성자 — 기본 TreeMap을 이용
    public TreeSet() {
        this.m = new TreeMap<E, Object>();
    }
    //Comparator를 받는 생성자
    public TreeSet(Comparator<? super E> comparator) {
        this.m = new TreeMap<E, Object>(comparator);
    }
    //다른 Collection으로부터 초기화
    public TreeSet(Collection<? extends E> c) {
        this.m = new TreeMap<E, Object>();
        addAll(c);
    }
    //요소 추가
    public boolean add(E e) {
        return m.put(e, PRESENT) == null;
    }
    //요소 제거
    public boolean remove(Object o) {
        return m.remove(o) == PRESENT;
    }
    //요소 포함 여부 확인
    public boolean contains(Object o) {
        return m.containsKey(o);
    }
    //전체 삭제
    public void clear() {
        m.clear();
    }
    //크기 반환
    public int size() {
        return m.size();
    }
    //반복자 (오름차순)
    public Iterator<E> iterator() {
        return m.navigableKeySet().iterator();
    }
    //내림차순 반복자
    public Iterator<E> descendingIterator() {
        return m.descendingKeySet().iterator();
    }
    //Comparator 반환
    public Comparator<? super E> comparator() {
        return m.comparator();
    }
    //첫 번째, 마지막 요소
    public E first() {
        return m.firstKey();
    }
    public E last() {
        return m.lastKey();
    }
}
```
- TreeMap이 실제로 트리를 관리하는 부분이다.
- TreeMap이 Red-Black Tree(균형 이진 탐색 트리) 구조를 사용한다.
```java
//TreeMap의 Entry(노드)
static final class Entry<K,V> implements Map.Entry<K,V> {
    K key; //key/value
    V value;
    Entry<K,V> left; //왼쪽, 오른쪽 자식 노드
    Entry<K,V> right;
    Entry<K,V> parent; //부모 노드
    boolean color = BLACK; //노드 색상 true=BLACK, false=RED
    Entry(K key, V value, Entry<K,V> parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
    }
}
//삽입 시 균형을 유지하도록 회전(rotations)과 색 변경이 일어난다.
/*
TreeSet
 └── TreeMap<E, Object>
       ├── Key  → TreeSet의 요소
       ├── Value → dummy object (PRESENT)
       └── Red-Black Tree
             ├── Node (key, left, right, parent, color)
             ├── color = Red/Black (균형 유지용)
             └── 회전/색 변경으로 트리 높이 유지
 */
```
- treeSet.add("B") 호출 시
```
1. TreeSet.add() → TreeMap.put("B", PRESENT) 호출
2. TreeMap이 루트부터 key 비교 (compareTo() 또는 Comparator)
3. 삽입 위치를 찾고 새 노드 생성 (Entry 객체)
4. Red-Black Tree 알고리즘으로 균형 유지 (회전 및 색상 조정)
5. 결과적으로 트리가 정렬된 상태 유지
-> 이 과정을 통해 TreeSet은 자동 정렬 + 중복 방지를 동시에 달성
```
- 사용 코드
```java
import java.util.*;
public class TreeSetInternalDemo {
    public static void main(String[] args) {
        //내림차순 정렬 TreeSet
        Set<Integer> set = new TreeSet<>(Comparator.reverseOrder());
        set.add(10);
        set.add(5);
        set.add(30);
        set.add(20);
        System.out.println(set); //[30, 20, 10, 5]
    }
}
```
- `[20, 10, 30, 25, 40]` 삽입 과정
- (1) 20 삽입
```css
20(B) /* 루트 노드는 항상 검정(Black) */
```
- (2) 10 삽입
```css
     20(B) /* 부모(20)는 검정이므로 균형 유지됨 (회전/색 변경 없음) */
     /
  10(R) /* 새 노드는 기본적으로 빨강(Red) */
```
- (3) 30 삽입
```css
      20(B) /* 부모(20)는 검정이므로 OK — 균형 유지됨 */
     /    \
  10(R)   30(R) /* 삽입된 노드(30)는 빨강 */
```
- (4) 25 삽입
```css
        20(B) /*  */
       /    \
    10(R)   30(R) /* 부모 30(R)도 빨강 → Red-Red conflict */
           /
        25(R) /* 새 노드 25(R) 삽입 */
```
```css
//색 변경 (Recoloring)
        20(R) /* 조부모(20)를 Red로 변경 */
       /    \
    10(B)   30(B) /* 부모(30)와 삼촌(10)을 Black으로 변경 */
           /
        25(R) /*  */
```
```css
//루트(20)는 다시 Black으로 유지
        20(B) /* 루트(20)는 다시 Black으로 */
       /    \
    10(B)   30(B) /*  */
           /
        25(R) /*  */
```
- (5) 40 삽입
```css
        20(B) /*  */
       /    \
    10(B)   30(B) /* 부모(30)는 Black → OK */
           /    \
        25(R)   40(R) /* 40(R) 추가 */
```
- TreeSet의 iterator()로 순회
```java
//중위순회(in-order traversal)를 하면 항상 정렬된 순서가 된다.
System.out.println(treeSet); //[10, 20, 25, 30, 40]
```
- 4. TreeSet 추가
- (1) dummy object 쓰는 이유
- TreeSet은 key만 필요, 값 자체가 키
```java
private transient NavigableMap<E, Object> m;
private static final Object PRESENT = new Object(); // 모든 key의 공통 value
//TreeSet.add(E e) → 내부적으로 TreeMap.put(e, PRESENT) 실행
//set.add("Apple") → map.put("Apple", PRESENT)
//set.add("Banana") → map.put("Banana", PRESENT)
```
- 모든 키가 같은 value 객체(PRESENT)를 공유
```java
//PRESENT는 단 하나의 Object 인스턴스 - 하나의 참조만 존재
value → [Object@0x123abc]
//value가 null이면 안 되니까 placeholder로 1개짜리 객체를 넣은 것
//메모리 절약됨
```
- (2) 왜 color를 쓰는가
- 이진 탐색 트리(BST)는 편향되면 성능이 O(n)까지 느려진다.
- Red-Black Tree는 색깔 규칙을 이용해서 트리의 균형을 유지
- 루트는 B, 새 노드는 R로 추가 후 균형 깨지면 색상 변경 또는 회전
```
- 루트 노드는 항상 Black
- 모든 리프(null 노드)는 Black으로 간주
- Red 노드의 자식은 항상 Black (Red-Red 연속 금지)
- 루트에서 각 리프까지 가는 모든 경로는 같은 수의 Black 노드를 가짐
//새 노드를 삽입할 때 항상 Red로 추가하는 이유
//B를 추가하면 모든 경로의 B 수 규칙이 깨진다.
```
- (3) Recoloring (색상 바꾸기)
```css
      20(B)
     /    \
  10(R)   30(R)
         /
       25(R) /* 새 노드(25)가 Red */
/* 부모(30)도 Red → Red-Red 연속 위반 */
/* 삼촌(10)이 Red → Recoloring 상황 */
```
```css
       /* Recoloring-1 (루트 규칙 불만족) */
      20(R)
     /    \
  10(B)   30(B)
         /
       25(R)
       /* Recoloring-2 (규칙 모두 만족) */
      20(B)
     /    \
  10(B)   30(B)
         /
       25(R)
```
- (4) Rotation (좌/우 회전)
- Red-Red 충돌을 트리 구조를 바꿔서 해소
- 상황: 삼촌이 Black이거나 존재하지 않을 때
```css
       /* Before */
     10(B)
        \
        20(R)
          \
          30(R)  ← 새로 삽입
       /* After Left Rotation */
       20(B)
      /   \
   10(R)  30(R)
/* 조부모(10) 기준으로 좌회전 수행 */
/* 부모(20)가 위로 올라감, 10이 왼쪽 자식이 됨 */
/* 색상도 교환: (20 Black, 10 Red) */
```
```css
       /* Before */
      30(B)
     /
   20(R)
  /
10(R)  ← 새로 삽입
       /* After Right Rotation */
       20(B)
      /   \
   10(R)  30(R)
/* 조부모(30) 기준으로 우회전 */
/* 20이 위로 올라감, 30이 오른쪽 자식이 됨 */
/* 색상 교환: (20 Black, 30 Red) */
```
- (5) Zigzag 케이스
- 부모와 자식이 삽입 방향이 엇갈리는 경우
- 트리를 먼저 한 번 회전시켜 직선으로 만든 뒤, 다시 회전
- Left-Right (LR) 패턴 (Right-Left도 있다.)
```css
       /* Before */
     30(B)
     /
   10(R)
     \
     20(R)
       /* Step 1 — 부모(10) 기준으로 좌회전 */
     30(B)
     /
   20(R)
   /
 10(R)
       /* Step 2 — 조부모(30) 기준으로 우회전 + 색상 변경 */
      20(B)
     /    \
  10(R)   30(R)
```
- (6) log n
- 이진 탐색 트리는 한 단계마다 데이터가 반으로 나뉘는 방식
- 알고리즘에서 log n은 밑이 2인 로그 (log₂ n)
- 즉, 2를 몇 번 곱해야 n이 되는가를 의미
```
표기 - 보통 의미 - 사용되는 분야
log₂ n - 밑이 2 - 컴퓨터 과학, 알고리즘
ln n - 밑이 e - 수학, 미적분
log₁₀ n - 밑이 10 - 공학, 로그 스케일 그래프
- 알고리즘 책에서 나오는 log n은 대부분 log₂ n
구분 - 의미 - 예
log₂ n - 이진 단계의 깊이 - 트리, 탐색
ln n - 미적분 계산용 - 연속 성장률
log₁₀ n - 인간이 직관적으로 이해하기 쉬운 크기 척도 - 데시벨
```
- 5. 이진 탐색 트리(BST) 순회
```css
        20
       /  \
     10    30
    /  \     \
   5   15    40
```
- (1) 전위 순회 (Preorder Traversal)
- 순서: 현재 노드 → 왼쪽 서브트리 → 오른쪽 서브트리
- 목적: 트리 구조를 복제하거나 출력할 때 사용
- 루트부터 먼저 방문하니까,
- 트리의 구조를 위에서 아래로 그대로 따라갈 수 있다.
- 트리를 파일로 저장하거나, 복사할 때
- 전위 순회 결과를 기반으로 트리를 다시 만들 수 있다.
```css
순회 결과: 20 → 10 → 5 → 15 → 30 → 40
1. Visit 20
 ├─ 2. Visit 10
 │   ├─ Visit 5
 │   └─ Visit 15
 └─ 3. Visit 30
     └─ Visit 40
```
- (2) 중위 순회 (Inorder Traversal)
- 순서: 왼쪽 서브트리 → 현재 노드 → 오른쪽 서브트리
- 목적: BST의 값을 오름차순 정렬된 순서로 출력
```css
순회 결과: 5 → 10 → 15 → 20 → 30 → 40
1. Traverse Left of 20
 ├─ Traverse Left of 10
 │   └─ Visit 5
 ├─ Visit 10
 ├─ Traverse Right of 10
 │   └─ Visit 15
2. Visit 20
3. Traverse Right of 20
 ├─ Visit 30
 └─ Visit 40
```
- (3) 후위 순회 (Postorder Traversal)
- 순서: 왼쪽 서브트리 → 오른쪽 서브트리 → 현재 노드
- 목적: 트리 삭제나 메모리 해제 시 사용
- 자식 노드를 먼저 처리하고 마지막에 부모 노드를 처리하니까,
- 의존 관계가 있는 구조를 안전하게 제거할 수 있다.
```css
순회 결과: 5 → 15 → 10 → 40 → 30 → 20
1. Traverse Left of 20
 ├─ Traverse Left of 10
 │   └─ Visit 5
 ├─ Traverse Right of 10
 │   └─ Visit 15
 └─ Visit 10
2. Traverse Right of 20
 ├─ Visit 40
 └─ Visit 30
3. Visit 20
```
- 6. Set - Iterator 메서드 활용
```java
    private static void iter(Set<String> set) {
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
    }
```
- `.hasNext()`: 다음 요소가 존재하는지 확인
- `.next()`: 현재 요소를 반환하고 내부 포인터를 다음 위치로 이동
- 7. 자바 HashSet 최적화
- 초기 capacity: 16
- Load Factor: 0.75 (75%)
- 리사이징: 2배로 확장
- rehashing 발생
```java
//HashSet 내부 (HashMap 사용)
static final int DEFAULT_INITIAL_CAPACITY = 16;
static final float DEFAULT_LOAD_FACTOR = 0.75f;
//threshold = capacity × load factor
//16 × 0.75 = 12개가 넘어가면 리사이징 발생
//리사이징 시:
//capacity: 16 → 32 → 64 → 128...
//모든 요소를 새 capacity 기준으로 rehashing
//HashSet이 O(1) 성능을 유지
```