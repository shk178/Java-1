- 6. 컬렉션 유틸
- (1) 불변 컬렉션 생성
```java
List<Integer> list1 = List.of(1, 2, 3);
Set<Integer> set1 = Set.of(1, 2, 3);
Map<Integer, String> map1 = Map.of(1, "c", 2, "b", 3, "a");
System.out.println(list1.getClass()); //class java.util.ImmutableCollections$ListN
System.out.println(set1.getClass()); //class java.util.ImmutableCollections$SetN
System.out.println(map1.getClass()); //class java.util.ImmutableCollections$MapN
```
- List.of(1, 2, 3) 같은 코드를 작성함 (팩토리 메서드 호출)
- 자바 내부에서 ImmutableCollections 클래스가 호출됨
- 요소 개수에 따라 다른 내부 클래스가 선택됨 (List12 or ListN)
- 만들어진 객체는 수정이 불가 (ArrayList가 아님, 수정 막아둠)
- (2) 가변 컬렉션으로 변경
```java
List<Integer> list2 = new ArrayList<>(list1); //복사
Set<Integer> set2 = new HashSet<>(set1);
Map<Integer, String> map2 = new HashMap<>(map1);
System.out.println(list2.getClass()); //class java.util.ArrayList
System.out.println(set2.getClass()); //class java.util.HashSet
System.out.println(map2.getClass()); //class java.util.HashMap
```
- (3) 빈 불변 리스트 생성
- Collections.emptyList();, List.of(); 다 된다.
- Collections.emptyList와 List.of가 호출하는 내부 클래스가 다르다.
- (4) Arrays.asList()
```java
List<Integer> list3 = Arrays.asList(1, 2, 3);
System.out.println(list3.getClass()); //class java.util.Arrays$ArrayList
//list3.add(0);
System.out.println(list3);
//Exception in thread "main" java.lang.UnsupportedOperationException
//	at java.base/java.util.AbstractList.add(AbstractList.java:155)
//	at java.base/java.util.AbstractList.add(AbstractList.java:113)
//	at comp.SortMain2.main(SortMain2.java:29)
```
- Arrays.asList()는 배열을 기반으로 한 리스트를 만든다.
- ArrayList가 아니라 java.util.Arrays$ArrayList라는 특수한 내부 클래스다.
- 배열의 크기를 바꿀 수 없어서 add()나 remove() 같은 메서드는 지원하지 않는다.
- 고정 크기 리스트, 값 변경은 가능하다.
- (5) 멀티스레드 동기화
- Collections.synchronizedList()
- Java에서 리스트를 스레드 안전하게 만들어주는 래퍼
- 여러 스레드가 동시에 같은 리스트에 접근할 때, 자동 동기화 처리해줌
- ArrayList, LinkedList 같은 컬렉션은 기본적으로 스레드 안전하지 않다.
```java
List<Integer> list = new ArrayList<>();
List<Integer> syncList = Collections.synchronizedList(list);
//syncList는 동기화된 리스트
//여러 스레드가 동시에 접근해도 안전하게 작동
```
- 내부적으로 모든 메서드 호출에 대해 동기화 블록(synchronized)을 걸어준다.
```java
public boolean add(E e) {
    synchronized (mutex) {
        return list.add(e);
    }
}
//모든 연산(get, set, add, remove 등)이 synchronized 블록 안에서 수행
//한 번에 하나의 스레드만 접근할 수 있다.
```
- 하지만, 반복(iteration)할 때는 수동으로 동기화해야 한다.
```java
List<Integer> syncList = Collections.synchronizedList(new ArrayList<>());
synchronized (syncList) {
    for (int num : syncList) {
        System.out.println(num);
    }
}
//iterator() 자체는 동기화되지 않는다.
//반복 중 다른 스레드가 수정하면 예외 발생
//ConcurrentModificationException
```
- 스레드 안전하지만, 단일 스레드 환경에서 속도가 느리고 스레드가 너무 많아지면 병목 생길 수 있다.
- java.util.concurrent 패키지의 컬렉션들을 써도 된다.
- CopyOnWriteArrayList → 읽기가 많고 쓰기가 적은 경우 빠름
- ConcurrentHashMap, ConcurrentLinkedQueue 등도 비슷한 목적