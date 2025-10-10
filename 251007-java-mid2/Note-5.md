# 10. 컬렉션 프레임워크 - Map, Stack, Queue
- 1. Map 소개1
- (1) Map 인터페이스
- 키(Key)와 값(Value)을 쌍으로 저장
- 키는 중복 불가, 값은 중복 가능
- 키를 통해 빠르게 값을 검색 가능
- 주요 메서드
```
put(K key, V value) - 키-값 쌍 추가
get(Object key) - 키에 해당하는 값 반환
remove(Object key) - 키-값 쌍 제거
containsKey(Object key) - 키 존재 여부 확인
containsValue(Object value) - 값 존재 여부 확인
keySet() - 모든 키 반환
values() - 모든 값 반환
entrySet() - 모든 키-값 쌍 반환
putIfAbsent(key, value) - 키 없을 때만 키-값 쌍 추가
```
- 구현 클래스
```
Map (인터페이스)
├── HashMap
│   └─ 가장 일반적인 구현체, 해시 기반, 순서 보장 X
├── LinkedHashMap extends HashMap
│   └─ 입력 순서 유지, 해시 기반
└── TreeMap
    └─ 키 기준 정렬, 이진 탐색 트리 기반
- HashMap: 정렬x, 빠름 (O(1)), 가장 많이 사용됨
- LinkedHashMap: 입력 순서로 정렬, 중간 성능, 순서 유지
- TreeMap: 키 기준 정렬, 느림 (O(log n)), 정렬 필요 시 유용
```
- 예시 코드
```java
public class MapMain {
    public static void main(String[] args) {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("A", 2);
        scores.put("B", 1);
        System.out.println(scores.get("A")); //2
        Set<String> keySet = scores.keySet();
        System.out.println(keySet); //[A, B]
        System.out.println(scores.values()); //[2, 1]
        //Set<Integer> vSet = scores.values();
        //java: incompatible types: java.util.Collection<java.lang.Integer> cannot be converted to java.util.Set<java.lang.Integer>
    }
}
//Collection은 Set이나 List의 상위 인터페이스이지만, 직접적으로 Set으로 캐스팅할 수는 없다.
Set<Integer> vSet = new HashSet<>(scores.values());
//scores.values()는 Collection<Integer>이므로, 이를 HashSet 생성자에 넣어주면 Set<Integer>로 변환된다.
List<Integer> vList = new ArrayList<>(scores.values());
//만약 순서가 중요하다면 List로 변환하는 것도 가능
```
- 2. Map 소개2
- (1) Map.Entry
- Map의 내부 인터페이스
- Map 안의 각 키-값 쌍을 표현하는 객체
- entrySet() 메서드를 통해 접근 가능
- 주요 메서드
```
getKey() - 키 반환
getValue() - 값 반환
setValue(V value) - 값 변경
```
- Map 인터페이스는 HashMap, TreeMap, LinkedHashMap 등 구현체가 있다.
- Map.Entry를 인터페이스로 정의해서
- Entry를 HashMap은 해시 기반으로, TreeMap은 정렬 기반으로 구현한다.
- (2) Map 특징
- 키-값 쌍 저장: 각 키는 하나의 값과 연결됨
- 키 중복 불가: 같은 키로 저장하면 기존 값이 덮어쓰기됨
- 값 중복 가능: 서로 다른 키가 같은 값을 가질 수 있음
- 빠른 검색: 키를 통해 빠르게 값 검색
- null 키 허용 여부: HashMap은 null 키 1개 허용
- null 값 허용 여부: HashMap은 null 값 허용
- HashMap은 순서 보장 X, LinkedHashMap은 입력 순서 유지, TreeMap은 키 기준 정렬
- entrySet() 제공: 키-값 쌍을 반복하며 처리 가능
- (3) entrySet()
- Map을 반복할 때 keySet() 대신
- entrySet()을 쓰면 더 유연하게 데이터를 다룰 수 있다.
```java
Set<Map.Entry<K, V>> entrySet = map.entrySet();
//entrySet()은 Set 형태로 반환되며, 각 요소는 Map.Entry<K, V> 타입
//for-each 문으로 반복하면서 getKey()와 getValue()를 사용할 수 있다.
```
- 예시 코드
```java
Map<String, Integer> scores = new HashMap<>();
scores.put("Alice", 90);
scores.put("Bob", 80);
scores.put("Charlie", 85);
for (Map.Entry<String, Integer> entry : scores.entrySet()) {
    System.out.println(entry.getKey() + " : " + entry.getValue());
}
//Alice : 90
//Bob : 80
//Charlie : 85
```
- (4) Map 인터페이스 메서드
- computeIfAbsent(key, k -> valueGenerator)
- 키가 존재하지 않으면
- valueGenerator 함수를 실행해서 값을 계산하고 저장
- 새로 계산된 값 또는 기존 값을 반환
```java
Map<String, List<String>> map = new HashMap<>();
//빈 맵을 만든다: {}
map.computeIfAbsent("fruits", k -> new ArrayList<>()).add("apple");
//"fruits" 키가 없다.
//new ArrayList<>()를 실행해서 빈 리스트를 만들고 저장한다.
//그 리스트를 반환받아서 .add("apple")로 "apple"을 추가한다.
//{fruits=[apple]}
map.computeIfAbsent("fruits", k -> new ArrayList<>()).add("banana");
//"fruits"가 있다.
//기존 리스트를 반환한다.
//그 리스트에 .add("banana")로 "banana"를 추가한다.
//{fruits=[apple, banana]}
/*
if (!map.containsKey("fruits")) {
    map.put("fruits", new ArrayList<>());
}
map.get("fruits").add("apple");
if (!map.containsKey("fruits")) {
    map.put("fruits", new ArrayList<>());
}
map.get("fruits").add("banana");
//를 실행한 것과 같은 결과다.
 */
```
- 3. HashMap
- (1) HashMap은 Entry(또는 Node) 객체를 사용해서 키-값 쌍을 저장
```java
//HashMap 내부 (간략화)
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;      //키의 해시코드
    final K key;         //키
    V value;             //값
    Node<K,V> next;      //다음 노드 (충돌 처리용)
}
```
- (2) HashMap은 키로 해시코드 생성
```java
HashMap<String, Integer> map = new HashMap<>();
map.put("apple", 100);
//내부 동작:
//1. "apple".hashCode() → 해시코드 생성
//2. 해시코드로 저장 위치 결정
//3. Entry("apple", 100) 객체를 그 위치에 저장
```
- (3) HashSet은 HashMap을 래핑한 것
```java
//HashSet 내부 (실제 구현 간략화)
public class HashSet<E> {
    private HashMap<E, Object> map = new HashMap<>();
    private static final Object PRESENT = new Object();  //더미 값
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }
}
HashSet<String> set = new HashSet<>();
set.add("apple");
//내부 동작:
//1. "apple".hashCode() → 해시코드 생성
//2. map.put("apple", PRESENT) 실행
//3. Entry("apple", PRESENT) 객체 저장
```
- (4) 정리
```
해시코드 생성
- HashMap: Key로 생성
- HashSet: 값 자체로 생성 (내부적으로는 Key로 사용)
저장 객체
- HashMap: Entry<K, V>
- HashSet: Entry<E, Object> (내부)
```
- 4. Stack 자료구조
- 후입 선출 LIFO, Last In First Out
- 스택에 값을 넣음: push
- 스택에서 값을 뺌: pop
- 스택에서 뺄 값 조회만: peek
- 자바 제공 Stack은 Vector를 사용한다.
- Vector는 예전 버전이어서, Stack 대신 Deque를 권장한다.
- 5. Deque 자료구조
- Double Ended Queue (덱, 데크)
- 양쪽 끝에서 추가/제거가 모두 가능한 자료구조
```
Queue = 앞에서만 제거, 뒤에서만 추가 (FIFO)
[앞] ← 제거    [  1  2  3  4  ] ← 추가 [뒤]
Deque = 양쪽에서 모두 추가/제거 가능
[앞] ↔ 추가/제거   [  1  2  3  4  ]  ↔ 추가/제거 [뒤]
```
- 기본 사용법
```java
Deque<Integer> deque = new ArrayDeque<>();
deque.addFirst(1);      //앞에 추가
deque.addLast(2);       //뒤에 추가
deque.offerFirst(3);    //앞에 추가 (실패시 false)
deque.offerLast(4);     //뒤에 추가 (실패시 false)
deque.removeFirst();    //앞에서 제거 (비어있으면 예외)
deque.removeLast();     //뒤에서 제거 (비어있으면 예외)
deque.pollFirst();      //앞에서 제거 (비어있으면 null)
deque.pollLast();       //뒤에서 제거 (비어있으면 null)
deque.getFirst();       //앞 요소 조회 (비어있으면 예외)
deque.getLast();        //뒤 요소 조회 (비어있으면 예외)
deque.peekFirst();      //앞 요소 조회 (비어있으면 null)
deque.peekLast();       //뒤 요소 조회 (비어있으면 null)
```
- 구현체 - ArrayDeque
```java
Deque<Integer> deque = new ArrayDeque<>();
//배열 기반, 빠름
//null 저장 불가 (NullPointerException)
```
- 구현체 - LinkedList
```java
Deque<Integer> deque = new LinkedList<>();
//연결 리스트 기반
//null 저장 가능
//ArrayDeque보다 느림
```
- 슬라이딩 윈도우 문제 해결됨
- 앞쪽: 오래된 데이터 제거 (pollFirst)
- 뒤쪽: 불필요한 데이터 제거 & 새 데이터 추가 (pollLast, addLast)
```
크기 3인 윈도우를 배열 위에서 오른쪽으로 이동하면서, 각 윈도우의 최댓값을 구하세요
배열: [1, 3, -1, -3, 5, 3, 6, 7]
윈도우 크기: 3
[1  3  -1] -3  5  3  6  7  → 최댓값: 3
 1 [3  -1  -3] 5  3  6  7  → 최댓값: 3
 1  3 [-1  -3  5] 3  6  7  → 최댓값: 5
 1  3  -1 [-3  5  3] 6  7  → 최댓값: 5
 1  3  -1  -3 [5  3  6] 7  → 최댓값: 6
 1  3  -1  -3  5 [3  6  7] → 최댓값: 7
결과: [3, 3, 5, 5, 6, 7]
```
```java
//단순한 방법 (느림)
//매번 윈도우 안에서 최댓값 찾기 - O(n*k)
for (int i = 0; i <= arr.length - k; i++) {
int max = arr[i];
    for (int j = i; j < i + k; j++) {
max = Math.max(max, arr[j]);
    }
            result.add(max);
}
```
```java
//덱을 사용한 방법 (빠름)
public int[] maxSlidingWindow(int[] nums, int k) {
    Deque<Integer> deque = new ArrayDeque<>(); //인덱스 저장
    int[] result = new int[nums.length - k + 1];
    for (int i = 0; i < nums.length; i++) {
        //1. 윈도우 범위를 벗어난 인덱스 제거 (앞에서)
        if (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
            deque.pollFirst();
        }
        //2. 현재 값보다 작은 값들을 뒤에서 제거 (현재 값이 더 크고 나중에 나가니까)
        while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
            deque.pollLast();
        }
        //3. 현재 인덱스 추가
        deque.addLast(i);
        //4. 윈도우가 완성되면 결과 저장 (맨 앞이 최댓값)
        if (i >= k - 1) {
            result[i - k + 1] = nums[deque.peekFirst()];
        }
    }
    return result;
}
배열: [1, 3, -1, -3, 5]
k = 3
i=0: deque=[0]             (값: 1)
i=1: deque=[1]             (값: 3) - 1 제거됨 (3이 더 큼)
i=2: deque=[1,2]           (값: 3,-1) → 결과[0]=3
i=3: deque=[1,2,3]         (값: 3,-1,-3) → 결과[1]=3
i=4: deque=[4]             (값: 5) - 다 제거됨 (5가 제일 큼) → 결과[2]=5
```
- 최대/최소값 유지 문제 해결됨
- 앞쪽: 항상 최댓값/최솟값 위치 (peekFirst)
- 뒤쪽: 새 값과 비교하며 정리 (pollLast, addLast)
```
데이터를 추가하면서 항상 현재까지의 최댓값을 O(1)로 알 수 있어야 해요
```
```java
public class MonotonicQueue {
    private Deque<Integer> deque = new ArrayDeque<>();
    //값 추가 (단조 감소 유지)
    public void push(int val) {
        //현재 값보다 작은 값들은 모두 제거 (val이 더 크니까 최댓값 후보에서 제외)
        while (!deque.isEmpty() && deque.peekLast() < val) {
            deque.pollLast();
        }
        deque.addLast(val);
    }
    //값 제거
    public void pop(int val) {
        if (!deque.isEmpty() && deque.peekFirst() == val) {
            deque.pollFirst();
        }
    }
    //현재 최댓값 조회
    public int max() {
        return deque.peekFirst();
    }
}
MonotonicQueue mq = new MonotonicQueue();
mq.push(1);     //deque=[1]
mq.max();       //1
mq.push(3);     //deque=[3]  (1 제거됨)
mq.max();       //3
mq.push(2);     //deque=[3,2]
mq.max();       //3
mq.pop(3);      //deque=[2]
mq.max();       //2
```
- 실전 활용 예시
```
최근 k일 중 주식 최고가를 실시간으로 알고 싶어요
Deque<int[]> deque = new ArrayDeque<>(); //[날짜, 가격]
최근 5게임 중 최고 점수를 표시하고 싶어요
Deque<Integer> scores = new ArrayDeque<>();
Deque를 사용하면 O(n) 시간에 이런 문제들을 해결할 수 있다.
```
- 6. 큐 자료구조
- FIFO, First In First Out
- 큐에 값을 넣음: offer
- 큐에서 값을 뺌: poll
- 큐에서 뺄 값 조회만: peek
```
PriorityQueue와 Deque는 형제 관계
        Collection
             |
          Queue
         /       \
    Deque    PriorityQueue
      |
  ArrayDeque
  LinkedList
```
```java
/*
PriorityQueue
- 자료구조: 힙
- 우선순위 순서 정렬
- 앞쪽 접근 가능: 최솟값
//Min Heap을 기본으로 사용
//다익스트라, 프림 알고리즘 등 알고리즘이 최솟값을 찾음
//자연스러운 순서 = 오름차순 = 최솟값이 먼저
//Comparable 인터페이스도 기본적으로 오름차순
- 뒤쪽 접근 불가
- 중간 제거: 비효율적
 */
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.add(30);
pq.add(10);
pq.add(20);
pq.poll(); //10 (항상 최솟값)
pq.poll(); //20
pq.poll(); //30
//우선순위 변경 - 최댓값 우선 = 내림차순
PriorityQueue<Integer> maxPQ = new PriorityQueue<>(Comparator.reverseOrder());
PriorityQueue<Integer> maxPQ = new PriorityQueue<>(Collections.reverseOrder());
PriorityQueue<Integer> maxPQ = new PriorityQueue<>((a, b) -> b - a);
//음수로 저장해도 내림차순 효과
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.add(-5); //5를 음수로
pq.add(-1); //1을 음수로
pq.add(-3); //3을 음수로
int max = -pq.poll(); //-(-5) = 5 (최댓값)
```
```java
/*
Deque
- 자료구조: 배열 또는 링크드리스트
- 추가한 순서 정렬
- 앞쪽 접근 가능
- 뒤쪽 접근 가능
- 중간 제거 불가
 */
Deque<Integer> deque = new ArrayDeque<>();
deque.addLast(30);
deque.addLast(10);
deque.addLast(20);
deque.pollFirst(); //30 (앞에서 제거)
deque.pollLast(); //20 (뒤에서 제거)
deque.pollFirst(); //10
```
- 큐 요소를 꺼내는 메서드가 2가지
```java
//poll() - 안전한 방식
//큐가 비어있으면 null 반환, 예외 던지지 않음
//remove() - 예외 발생 방식
//큐가 비어있으면 런타임 오류 발생
public class QueueMain {
    public static void main(String[] args) {
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        System.out.println(queue.peek()); //1
        System.out.println(queue.peek()); //1
        System.out.println(queue.poll()); //1
        System.out.println(queue.poll()); //2
        System.out.println(queue.poll()); //3
        System.out.println(queue.poll()); //null (연결 리스트여서가 아니다.)
        //System.out.println(queue.remove());
        //Exception in thread "main" java.util.NoSuchElementException
        //	at java.base/java.util.LinkedList.removeFirst(LinkedList.java:281)
        //	at java.base/java.util.LinkedList.remove(LinkedList.java:696)
        //	at msq.QueueMain.main(QueueMain.java:16)
    }
}
```
- 예외 반환: add(e), remove(), element()
- null/false 반환: offer(e), poll(), peek()
- 7. ArrayDeque 원형 구조
- 배열의 끝과 시작이 연결되어 있다.
```java
//일반 배열로 큐 만들기
int[] arr = new int[5];
int front = 0; //큐의 앞을 가리키는 인덱스
//front=삭제 위치=큐에서 데이터를 꺼낼 때 참조
int rear = 0; //큐의 뒤를 가리키는 인덱스
//rear=삽입 위치=큐에 데이터를 넣을 때 참조
//요소 추가 - rear 위치에 값을 넣고, rear를 1 증가시킴
arr[rear++] = 1; //[1, _, _, _, _] rear=1
arr[rear++] = 2; //[1, 2, _, _, _] rear=2
arr[rear++] = 3; //[1, 2, 3, _, _] rear=3
//요소 제거 - front 위치의 데이터를 꺼내고, front를 1 증가시킴
arr[front++] = 0; //[0, 2, 3, _, _] front=1
arr[front++] = 0; //[0, 0, 3, _, _] front=2
//계속 추가하면...
arr[rear++] = 4; //[0, 0, 3, 4, _] rear=4
arr[rear++] = 5; //[0, 0, 3, 4, 5] rear=5
arr[rear++] = 6; //rear가 5를 넘어감, 공간은 있는데 못 씀
//배열은 고정된 크기라서 rear가 끝까지 가면 더 이상 데이터를 넣을 수 없다.
//하지만 front가 앞쪽 데이터를 제거했기 때문에 빈 공간은 존재
//앞쪽에 빈 공간이 있는데도 못 쓰게 된다.
```
```java
//원형 배열 구현
int[] arr = new int[5];
int front = 0;
int rear = 0;
int size = 0;
//추가할 때: rear = (rear + 1) % arr.length;
//+1은 다음 위치로 이동하기 위한 것
//% arr.length는 배열 범위를 벗어나지 않게 하기 위한 것
//다음 위치로 이동하되, 배열 끝을 넘으면 처음으로 돌아가라
void add(int value) {
    arr[rear] = value;
    rear = (rear + 1) % 5;
    size++;
}
//제거할 때: front = (front + 1) % arr.length;
int remove() {
    int value = arr[front];
    front = (front + 1) % 5;
    size--;
    return value;
}
```
- ArrayDeque 실제 구현
```java
public class ArrayDeque<E> {
    private Object[] elements;
    private int head; //front
    private int tail; //rear
    public void addLast(E e) {
        elements[tail] = e;
        tail = (tail + 1) & (elements.length - 1); //% 대신 비트 연산
        if (tail == head) {
            doubleCapacity(); //꽉 차면 크기 2배 증가
        }
    }
    public E pollFirst() {
        int h = head;
        E result = (E) elements[h];
        if (result == null) return null;
        elements[h] = null;
        head = (h + 1) & (elements.length - 1); //원형으로 이동
        return result;
    }
}
```
- LinkedList
```
//노드 연결
[1] → [2] → [3]
//제거하면 노드 삭제
[2] → [3]
//추가하면 새 노드 생성
[2] → [3] → [4]
메모리 할당/해제 필요
포인터로 연결 (메모리 오버헤드)
```
- 8. 문제 풀이
- `Map<String, Integer> map = Map.of("A", 1, "B", 2, "C", 3);`
- Map.of로 생성한 Map은 불변이다.
- `map.computeIfAbsent(word, k -> 0)`: 조회 -> 없으면 값이 Map에 저장됨
- `map.getOrDefault(word, 0)`: 조회 -> 없으면 기본값만 반환함