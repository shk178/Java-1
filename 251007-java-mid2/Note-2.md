# 4. 컬렉션 프레임워크 - ArrayList
- 1. 배열
```java
public class Main {
    public static void main(String[] args) {
        int[] arr = new int[3];
        //index 입력: O(1)
        arr[0] = 1;
        arr[1] = 2;
        arr[2] = 3;
        //index 변경: O(1)
        arr[2] = 10;
        //index 조회: O(1)
        System.out.println(arr[2]); //10
        //arr 검색: O(n)
        int value = 10;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) break;
        }
    }
}
```
- 메모리 주소는 항상 바이트 단위로 표현
- 각 데이터 타입의 크기만큼 주소가 증가
- 16진수 주소에서 앞의 x(또는 0x)는 16진법을 나타내는 접두사
- arr 주소 = arr[0] 주소 = x100
- arr[1] 주소 = x104
- arr[2] 주소 = x108
- 배열 순차 검색: 배열 크기 n = 연산 최대 n회
- 2. 빅오 표기법
- n은 데이터 크기
- 연산 수 n+2 -> O(n), 연산 수 n/2 -> 0(n)
- O(1): 연산 수가 항상 1 //상수 시간
- O(log n): 연산 수가 log n //로그 시간
- O(n): 연산 수가 n //선형 시간
- O(n log n): n*log n //선형 로그 시간
- O(n^2): n 제곱 //제곱 시간
- n이 클 때: 1 < log n < n < n log n < n^2
- 빅오로 알고리즘 실행 시간 계산보다는
- 데이터 증가에 따른 성능 변화 추세를 본다.
- 3. 배열 데이터 추가
- 배열 위치 i에 데이터 추가
- i부터 arr.length-1까지 오른쪽으로 한 칸씩 이동
- 배열 끝 위치부터 이동해야 데이터 유지
- arr.length-1 -> arr.length로 이동은 런타임 오류 난다. (크기 고정)
- i가 arr.length-1이면 O(1)이다.
- 그외에는 O(n)이다.
- 4. List 자료 구조
- 배열은 크기 정적이고, 데이터 추가 시 이동하는 코드를 작성해야 한다.
- 리스트는 크기 동적이고, 추가 메서드가 있다.
- 배열과 리스트 모두 순서가 있고 중복을 허용한다.
- 5. 251007-java-mid2/src/array1/arrList3.java
- (1) shiftRightFrom(index)
```java
for (int i = size; i > index; i--) {
    elementData[i] = elementData[i - 1]; //i 위치에 왼쪽 원소를 쓴다.
    //왼쪽 원소가 오른쪽으로 이동
    //배열 끝 원소부터 i=size 오른쪽으로 이동
    //index 원소가 i=index+1 위치로 이동까지
}
//size=5, index=4, i=5
//size=5, index=2, i=5, 4, 3
//size=5, index=0, i=5, 4, 3, 2, 1
```
- (2) shiftLeftFrom(index)
```java
for (int i = index; i < size - 1; i++) {
    elementData[i] = elementData[i + 1]; //i 위치에 오른쪽 원소를 쓴다.
    //오른쪽 원소가 왼쪽으로 이동
    //index+1 원소부터 i=index 왼쪽으로 이동
    //size-1 원소가 i=size-2 위치로 이동까지
}
//size=5, index=4, 이동x
//size=5, index=2, i=2, 3
//size=5, index=0, i=0, 1, 2, 3
```
- (3) Arrays.copyOf(elementData, size)
- 배열 elementData의 앞부분(size개) 만 잘라서 새 배열을 만든다.
- (4) Arrays.toString( ... )
- 배열을 문자열로 바꿔서 `[`와 `]` 포함된 한 줄로 출력한다.
- (5) 타입 안정성
- Object로 담고 반환하는 코드
- 다운 캐스팅 중 런타임 오류 발생 가능성
- 6. 251007-java-mid2/src/array1/arrList4.java
- (1) Object 배열 사용
- 제네릭 타입은 `new E[]`로 배열 직접 생성 불가
- 배열은 공변적이다.
```java
//String이 Object의 하위 타입이라면
//String[]도 Object[]의 하위 타입으로 인정
String[] strings = new String[5];
Object[] objects = strings; //허용 (공변성)
//배열은 런타임에도 타입 정보를 가지고 있어서
//런타임에 타입 체크해서
//실제 타입(String[])과 다르면 오류 낸다.
objects[0] = 123; //ArrayStoreException
```
- 제네릭은 런타임에 타입 소거된다.
```java
//컴파일 중 (타입 체크됨)
ArrayList<String> list1 = new ArrayList<>();
ArrayList<Integer> list2 = new ArrayList<>();
//컴파일 후 (런타임에 타입 체크 안 됨)
ArrayList list1 = new ArrayList();
ArrayList list2 = new ArrayList();
//ArrayList 같은 제네릭 컬렉션은 내부적으로 Object[] 배열 사용
```
- (2) Object -> E로 캐스팅 시점
- 반환 시 캐스팅보다 변수 선언 시 캐스팅이 낫다.
```java
return (E) temp;
//가능하지만 덜 선호됨
E temp = (E) elementData[index];
//temp는 E 타입 - 다른 E 타입 변수에 할당 가능
E temp = get(index);
//get() 메서드 재사용이 더 낫다.
//코드 중복 제거 - 캐스팅 로직을 get()에서만 관리
elementData[i] = elementData[i - 1];
//shift는 내부 배열 조작만 하는 private 메서드
//Object[] 내에서 Object 끼리만 이동하면 됨
//elementData[i - 1]를 get으로 하면 불필요한 캐스팅 오버헤드
//내부 구현에서 성능이 중요하고, 타입 변환이 불필요한 경우 배열 직접 접근
```
- (3) SuppressWarnings 사용
```java
@SuppressWarnings("unchecked")
public E get(int index) {
    return (E) elementData[index];
}
//컴파일러 unchecked 경고 이유
//elementData는 Object[]
//(E) 캐스팅이 런타임에 안전한지 보장할 수 없음
//잘못된 타입이 들어있으면 ClassCastException 발생 가능
public E get(int index) {
    @SuppressWarnings("unchecked")
    E result = (E) elementData[index];
    return result;
} //특정 라인에만 적용 권장
```
- (4) arrList 단점
- [size]부터는 낭비되는 메모리다.
- 앞/중간에 데이터 추가/삭제 시 O(n)이다.
# 5. 컬렉션 프레임워크 - LinkedList
- 1. 노드와 연결
- arrList 단점 보완 = linkedList
- 251007-java-mid2/src/linked1/Node.java
- 251007-java-mid2/src/linked1/linkedList.java
- linkedList 단점 = index로 접근이 O(n)이다.
- 2. 성능
- get(index): 배열리스트 O(1), 연결리스트 O(n)
- indexOf(item): 배열리스트 O(n), 연결리스트 O(n)
- 앞에 추가/삭제: 배열리스트 O(n), 연결리스트 O(1)
- 중간에 추가/삭제: 배열리스트 O(n), 연결리스트 O(n)
- 뒤에 추가/삭제: 배열리스트 O(1), 연결리스트 O(n)
- 3. 자바 리스트
- 자바가 제공하는 배열 리스트
- 251007-java-mid2/zip/ArrayList.java
- 자바가 제공하는 연결 리스트는 이중 연결 리스트
- 251007-java-mid2/zip/LinkedList.java
- 4. 제네릭 연결 리스트
- 내부 클래스가 외부 클래스의 인스턴스 멤버에 접근할 필요가 없다면 static으로 선언
- 251007-java-mid2/src/linked1/gLinkedList.java
- 타입 안정성이 높다.
# 6. 컬렉션 프레임워크 - List
- 1. 리스트 추상화1 - 인터페이스 도입
- 리스트 자료구조: 순서가 있고, 중복을 허용
- 배열 리스트, 연결 리스트 공통 기능을 인터페이스로 추상화할 수 있다.
- 추상화하면 다형성 활용할 수 있다.
- 251007-java-mid2/src/list/listInterface.java
- arrList4.java, gLinkedList.java가 구현
```java
public class Main1 {
    public static void main(String[] args) {
        listInterface<String> list1 = new arrList4<>();
        listInterface<Integer> list2 = new gLinkedList<>();
    }
}
//오버라이딩한 메서드만 list1, list2로 쓸 수 있다.
```
- 2. 리스트 추상화2 - 의존관계 주입
- (1) 배치 프로세서: 앞에 추가할 땐 연결리스트, 뒤에 추가할 땐 배열리스트로 바꿈
- 배치 프로세서가 구체적인 클래스에 의존하고 있다.
- (2) 배치 프로세서가 추상적인 인터페이스에 의존하게 할 수 있다.
- 이때 실행 시점에 생성자를 통해서 어떤 구현체일지가 결정된다.
- (3) 배치 프로세서의 의존관계가 외부에서 주입되는 것 같다고 해서
- 의존관계 주입(DI)이라고 한다.
- 생성자 통하면 생성자 의존관계 주입 또는 생성자 주입이라 한다.
- (4) 생성자 주입
- 의존 객체를 생성자의 매개변수로 전달
- 불변성 보장, 테스트 용이, 가장 권장됨
```java
public class BatchProcessor {
    private listInterface<Integer> list;
    public BatchProcessor(listInterface<Integer> list) {
        this.list = list;
    }
}
```
- (5) 세터(Setter) 주입
- setList(...) 같은 메서드로 외부에서 주입
- 의존관계 변경 가능, 유연하지만 불완전 객체 가능성
```java
public class BatchProcessor {
    private listInterface<Integer> list;
    public void setList(listInterface<Integer> list) {
        this.list = list;
    }
}
```
- (6) 필드 주입
- 필드에 직접 대입하거나 어노테이션(@Autowired)으로 주입
- 간단하지만 테스트 어렵고 결합도 높음
```java
public class BatchProcessor {
    @Autowired
    private listInterface<Integer> list;
}
```
- 3. 리스트 추상화3 - 컴파일 타임/런타임 의존관계
- (1) 컴파일 타임 의존관계
- 클래스에 보이는, 실행하지 않은 코드에 정적으로 나타나는 의존관계
- 배치 프로세서는 listInterface만 사용한다: 해당 인터페이스에 의존
```java
private listInterface<Integer> list;
```
- (2) 런타임 의존관계
- 프로그램 실행 중 보이는, 인스턴스 간에 나타나는 의존관계
- 주로 생성된 인스턴스와 그것을 참조하는 의존관계
```java
listInterface<Integer> arr = new arrList4<>();
listInterface<Integer> linked = new gLinkedList<>();
BatchProcessor batch1 = new BatchProcessor(arr); //배열 리스트 주입
BatchProcessor batch2 = new BatchProcessor(linked); //연결 리스트 주입
//batch1은 arrList4와 의존관계를 맺는다.
//batch2는 gLinkedList와 의존관계를 맺는다.
```
- OCP 원칙, 전략 패턴: 클라이언트 코드 유지, 알고리즘 변경
- (3) 런타임 의존관계는 실행 중 바뀔 수 있다.
- 의존하는 구체 클래스가 프로그램 실행 중에 바뀔 수 있다는 뜻
- 의존 객체를 바꾸면 실제 동작이 달라진다.
```java
listInterface<Integer> arr = new arrList4<>();
BatchProcessor batch1 = new BatchProcessor(arr); //배열 리스트 생성자 주입
batch1.setList(new gLinkedList<>()); //연결 리스트 세터 주입
```
- (4) 런타임 의존관계 교체 시 주의
- 객체의 일관성이 깨질 수 있음
- 동시성(멀티스레드) 환경에서는 위험
- 불변성이 깨지므로 설계 안정성 낮음
- 실무에서는 프로그램 시작 시점에 한 번 주입하고,
- 그 이후에는 불변으로 유지하도록 final 쓰는 게 일반적
- 4. 직접 구현한 리스트의 성능 비교
- Runnable: 자바의 함수형 인터페이스
```java
@FunctionalInterface
public interface Runnable {
    void run(); //매개변수도 없고, 리턴값도 없는 메서드
}
```
- Runnable 사용법
```java
//람다식으로 사용
Runnable task = () -> {
    System.out.println("Hello!");
};
task.run();
//익명 클래스로 사용
Runnable task2 = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello!");
    }
};
```
- 배치 프로세서에 Runnable 적용
```java
private void measurePerformance(String methodName, Runnable logic, int size) {
    long sTime = System.currentTimeMillis();
    logic.run(); //전달받은 코드 실행
    long eTime = System.currentTimeMillis();
    System.out.print(methodName + " list=" + list.getClass().getName());
    System.out.print(" size=" + size);
    System.out.println(" time=" + (eTime - sTime));
}
public void logicFront(int size) {
    //() -> { ... }가 Runnable 객체를 만드는 람다식
    measurePerformance("logicFront", () -> {
        for (int i = 0; i < size; i++) {
            list.add(0, i);
        }
    }, size);
}
public void logicMiddle(int size) {
    measurePerformance("logicMiddle", () -> {
        for (int i = 0; i < size; i++) {
            list.add(list.size() / 2, i);
        }
    }, size);
}
public void logicBack(int size) {
    measurePerformance("logicBack", () -> {
        for (int i = 0; i < size; i++) {
            list.add(list.size(), i); //shift 없이 맨 뒤에 추가만
        }
    }, size);
}
```
- 251007-java-mid2/src/list/BatchProcessor.java
```
logicFront (맨 앞 삽입)
- ArrayList O(n)*루프 n회-이동 n^2번
- LinkedList O(1)*루프 n회-접근 n번
logicMiddle (중간 삽입)
- ArrayList O(n/2)*루프 n회-이동 n^2/2번
- LinkedList O(n/2)*루프 n회-탐색 n^2/2번
logicBack (맨 뒤 삽입)
- ArrayList O(1)*루프 n회-접근 n번 + 용량 확장
- LinkedList O(n)*루프 n회-탐색 n^2번
logicIndex
- ArrayList O(n)*루프 n회-비교 n^2번 + 캐시 고효율(연속 메모리)
- LinkedList O(1)*루프 n회-탐색 n^2번 + 캐시 저효율(분산 메모리)
logicGet, logicSet
- ArrayList O(1)*루프 n회-접근 n번
- LinkedList O(n)*루프 n회-탐색 n^2번
//LinkedList는 tail 없으면 탐색 느리다.
```
- 5. 자바 리스트
- (1) `<interface> Collection`
- `java.util.Collection<E>`
- 자바 컬렉션 프레임워크의 최상위 인터페이스 (Map 제외)
- 데이터 집합(objects group)을 표현하기 위한 가장 일반적인 형태
- 중복 요소 허용, 순서 보장 여부는 하위 인터페이스나 구현 클래스에 따라 다름
- 제네릭을 사용해 타입 안정성을 제공
- 주요 메서드
```
메서드	설명
boolean add(E e)	요소 추가
boolean remove(Object o)	요소 제거
void clear()	모든 요소 제거
boolean contains(Object o)	요소 포함 여부 확인
int size()	요소 개수 반환
boolean isEmpty()	비어 있는지 확인
Iterator<E> iterator()	반복자 반환 (for-each 가능)
```
- 주요 하위 인터페이스
```
인터페이스	특징
List<E>	순서가 있고, 중복 허용
Set<E>	순서가 없고, 중복 허용하지 않음
Queue<E>	FIFO(선입선출) 구조, 순서 기반 처리
```
- (2) `<interface> List`
- `java.util.List<E>`
- Collection을 상속받은 하위 인터페이스
- 저장 순서 유지 (ordered)
- 중복 요소 허용 (duplicate allowed)
- 인덱스(index) 로 접근 가능 (get(int index))
- 주요 메서드
```
메서드	설명
void add(int index, E element)	특정 위치에 요소 추가
E get(int index)	인덱스로 요소 가져오기
E set(int index, E element)	특정 위치의 요소 교체
int indexOf(Object o)	처음 일치하는 요소의 인덱스
int lastIndexOf(Object o)	마지막으로 일치하는 요소의 인덱스
List<E> subList(int from, int to)	부분 리스트 반환
```
- 주요 구현 클래스
```
클래스	특징
ArrayList<E>	배열 기반, 빠른 접근, 삽입/삭제 느림
LinkedList<E>	연결 리스트 기반, 삽입/삭제 빠름, 접근 느림
Vector<E>	예전 클래스 (동기화 지원), 거의 사용 안 함
Stack<E>	Vector 기반의 LIFO 구조 클래스
```
- (3) 계층 구조
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
  ├── Set
  │     ├── HashSet
  │     ├── LinkedHashSet
  │     └── TreeSet
  │
  └── Queue
        ├── PriorityQueue
        └── LinkedList
```
- (4) 왜 Collection을 상속받는가
- 자바 컬렉션 프레임워크(JCF, Java Collection Framework)는
- 자료구조를 추상화해서 일관된 방식으로 다루자는 생각
- Collection은 모든 요소 집합의 공통 규약
- 요소 추가/삭제, 크기 확인, 포함 여부, 전체 순회
- List는 순서 있는 데이터로 순서 유지, 중복 허용
- Set은 집합 형태 데이터로 순서 없음, 중복 안 됨
- Queue는 대기열 구조 (FIFO)로 삽입 순서, 중복 허용
- List/Set/Queue의 행동 방식 다르지만 공통 규약을 따른다.
- Collection 타입 하나로 모두 다룰 수 있다.
```
설계 목적	설명
일관성 (Consistency)	모든 컬렉션이 같은 기본 동작(add, remove 등)을 제공
유연성 (Flexibility)	구현체(ArrayList, HashSet 등)를 쉽게 교체 가능
추상화 (Abstraction)	“자료구조의 종류”보다 “데이터의 집합” 개념에 집중
확장성 (Extensibility)	새로운 컬렉션 타입을 추가해도 공통 규약 유지
```
- (5) `java.util.ArrayList`
- 배열 사용해 데이터 관리
- 기본 CAPACITY = 10, 넘어가면 50%씩 증가
- 한 칸씩 옮기지 않고, 메모리 고속 복사(System.arraycopy()) 수행
- (6) `java.util.LinkedList`
- 이중 연결 리스트 구조
- head, tail 노드 둘 다 참조
- 앞/뒤에서부터 조회 가능: 인덱스가 앞에 가까우면 앞에서부터 조회
- 6. 자바 리스트의 성능 비교
- 251007-java-mid2/src/list/BatchProcessor2.java
- 251007-java-mid2/src/list/Main3.java
- ArrayList는 모든 add에서 빨라졌다.
- LinkedList는 back add에서 빨라졌다.
- indexOf, get, set은 코드를 봐야 알 것 같다.
- ArrayList가 연속 메모리 구조여서 퍼포먼스가 좋다.