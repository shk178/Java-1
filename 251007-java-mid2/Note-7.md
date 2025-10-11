- 5. 정렬 - Comparable, Comparator 2
- (1) 자바 자료구조와 Comparable/Comparator
```
자료구조
├─ 정렬 필요 없음 (순서 무관)
│   ├─ HashMap
│   ├─ HashSet
│   └─ Hashtable
│
├─ 자동 정렬 (Comparable/Comparator 필수)
│   ├─ TreeSet      ← Comparable 또는 Comparator 필수
│   ├─ TreeMap      ← Comparable 또는 Comparator 필수
│   └─ PriorityQueue ← Comparable 또는 Comparator 필수
│
└─ 수동 정렬 (Collections.sort/Arrays.sort 호출)
    ├─ ArrayList    ← Comparable 또는 Comparator 사용
    ├─ LinkedList   ← Comparable 또는 Comparator 사용
    ├─ Vector       ← Comparable 또는 Comparator 사용
    └─ 배열 (Array)  ← Comparable 또는 Comparator 사용
```
- List - 수동 정렬 - ArrayList, LinkedList, Vector
```java
class Student implements Comparable<Student> {
    String name;
    int score;
    public Student(String name, int score) {
        this.name = name;
        this.score = score;
    }
    @Override
    public int compareTo(Student other) {
        return this.score - other.score; //점수 오름차순
    }
    @Override
    public String toString() {
        return name + "(" + score + ")";
    }
}
/* Comparable 사용 */
List<Student> students = new ArrayList<>();
students.add(new Student("Alice", 85));
students.add(new Student("Bob", 92));
students.add(new Student("Charlie", 78));
//Comparable의 compareTo() 사용
Collections.sort(students);
//또는
students.sort(null); //null이면 Comparable 사용
System.out.println(students);
//[Charlie(78), Alice(85), Bob(92)]
/* Comparator 사용 */
//이름순 정렬
Collections.sort(students, (s1, s2) -> s1.name.compareTo(s2.name));
//또는
students.sort(Comparator.comparing(s -> s.name));
//점수 내림차순
students.sort((s1, s2) -> s2.score - s1.score);
//또는
students.sort(Comparator.comparing(Student::getScore).reversed());
System.out.println(students);
/*  Comparable도 Comparator도 없으면 */
class Person {
    String name;
    int age;
    //Comparable 구현 안 함
}
List<Person> people = new ArrayList<>();
people.add(new Person("Alice", 25));
people.add(new Person("Bob", 30));
Collections.sort(people); //컴파일 에러
//Person cannot be cast to java.lang.Comparable
//해결: Comparator 제공
people.sort((p1, p2) -> p1.age - p2.age); //가능
```
- TreeSet - 자동 정렬 (Comparable 또는 Comparator 필수)
```java
/* 방법 1: Comparable 사용 */
class Student implements Comparable<Student> {
    String name;
    int score;
    @Override
    public int compareTo(Student other) {
        return this.score - other.score;
    }
}
TreeSet<Student> students = new TreeSet<>();
students.add(new Student("Alice", 85));
students.add(new Student("Bob", 92));
students.add(new Student("Charlie", 78));
System.out.println(students);
//[Charlie(78), Alice(85), Bob(92)] - 자동 정렬됨
/* 방법 2: Comparator 사용 (생성자에 전달) */
class Student {
    String name;
    int score;
    //Comparable 구현 안 함
}
//이름순 TreeSet
TreeSet<Student> byName = new TreeSet<>(
    (s1, s2) -> s1.name.compareTo(s2.name)
);
//점수 내림차순 TreeSet
TreeSet<Student> byScore = new TreeSet<>(
    (s1, s2) -> s2.score - s1.score
);
//또는
TreeSet<Student> byScore2 = new TreeSet<>(
    Comparator.comparing(Student::getScore).reversed()
);
byName.add(new Student("Charlie", 78));
byName.add(new Student("Alice", 85));
byName.add(new Student("Bob", 92));
System.out.println(byName);
//[Alice(85), Bob(92), Charlie(78)] - 이름순
/*  Comparable도 Comparator도 없으면 */
class Person {
    String name;
    int age;
    //Comparable 구현 안 함
}
TreeSet<Person> people = new TreeSet<>();
people.add(new Person("Alice", 25)); //런타임 에러
//ClassCastException: Person cannot be cast to java.lang.Comparable
//해결 1: Comparable 구현
class Person implements Comparable<Person> { ... }
//해결 2: Comparator 제공
TreeSet<Person> people = new TreeSet<>(
    (p1, p2) -> p1.name.compareTo(p2.name)
);
/* TreeSet 특수 메서드 */
TreeSet<Integer> numbers = new TreeSet<>();
numbers.add(5);
numbers.add(2);
numbers.add(8);
numbers.add(1);
System.out.println(numbers); //[1, 2, 5, 8]
System.out.println(numbers.first()); //1 (최솟값)
System.out.println(numbers.last()); //8 (최댓값)
System.out.println(numbers.lower(5)); //2 (5보다 작은 것 중 최대)
System.out.println(numbers.higher(5)); //8 (5보다 큰 것 중 최소)
System.out.println(numbers.headSet(5)); //[1, 2] (5 미만)
System.out.println(numbers.tailSet(5)); //[5, 8] (5 이상)
```
- TreeMap - 자동 정렬 (Key에 Comparable 또는 Comparator 필요)
```java
/* 방법 1: Key가 Comparable 구현 */
//Integer는 Comparable 구현됨
TreeMap<Integer, String> map = new TreeMap<>();
map.put(3, "Three");
map.put(1, "One");
map.put(2, "Two");
System.out.println(map);
//{1=One, 2=Two, 3=Three} - Key 기준 자동 정렬
//String도 Comparable 구현됨
TreeMap<String, Integer> scores = new TreeMap<>();
scores.put("Charlie", 78);
scores.put("Alice", 85);
scores.put("Bob", 92);
System.out.println(scores);
//{Alice=85, Bob=92, Charlie=78} - Key(이름) 사전순
/* 방법 2: Comparator 사용 */
class Student {
    String name;
    int id;
    public Student(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
//ID 기준 정렬
TreeMap<Student, Integer> scoreMap = new TreeMap<>(
    (s1, s2) -> s1.id - s2.id
);
//이름 기준 정렬
TreeMap<Student, Integer> scoreMap2 = new TreeMap<>(
    Comparator.comparing(s -> s.name)
);
scoreMap.put(new Student("Alice", 3), 85);
scoreMap.put(new Student("Bob", 1), 92);
scoreMap.put(new Student("Charlie", 2), 78);
//ID 순으로 정렬됨 (1, 2, 3)
/* Comparable도 Comparator도 없으면 */
class Person {
    String name;
    //Comparable 구현 안 함
}
TreeMap<Person, Integer> map = new TreeMap<>();
map.put(new Person("Alice"), 25); //런타임 에러
//ClassCastException
//해결: Comparator 제공
TreeMap<Person, Integer> map = new TreeMap<>(
    (p1, p2) -> p1.name.compareTo(p2.name)
);
/* TreeMap 특수 메서드 */
TreeMap<Integer, String> map = new TreeMap<>();
map.put(1, "One");
map.put(5, "Five");
map.put(3, "Three");
System.out.println(map.firstKey()); //1
System.out.println(map.lastKey()); //5
System.out.println(map.lowerKey(3)); //1
System.out.println(map.higherKey(3)); //5
System.out.println(map.headMap(3)); //{1=One}
System.out.println(map.tailMap(3)); //{3=Three, 5=Five}
```
- PriorityQueue - 자동 부분 정렬 (Comparable 또는 Comparator 필요)
```java
/* 방법 1: Comparable 사용 (최소 힙) */
class Task implements Comparable<Task> {
    String name;
    int priority; //낮을수록 우선
    @Override
    public int compareTo(Task other) {
        return this.priority - other.priority;
    }
    @Override
    public String toString() {
        return name + "(" + priority + ")";
    }
}
PriorityQueue<Task> queue = new PriorityQueue<>();
queue.offer(new Task("작업C", 3));
queue.offer(new Task("작업A", 1));
queue.offer(new Task("작업B", 2));
System.out.println(queue.poll()); //작업A(1) - 우선순위 가장 높음
System.out.println(queue.poll()); //작업B(2)
System.out.println(queue.poll()); //작업C(3)
/* 방법 2: Comparator 사용 */
class Task {
    String name;
    int priority;
    //Comparable 구현 안 함
}
//최소 힙 (우선순위 낮은 것부터)
PriorityQueue<Task> minHeap = new PriorityQueue<>(
    (t1, t2) -> t1.priority - t2.priority
);
//최대 힙 (우선순위 높은 것부터)
PriorityQueue<Task> maxHeap = new PriorityQueue<>(
    (t1, t2) -> t2.priority - t1.priority
);
//또는
PriorityQueue<Task> maxHeap2 = new PriorityQueue<>(
    Comparator.comparing(Task::getPriority).reversed()
);
maxHeap.offer(new Task("작업C", 3));
maxHeap.offer(new Task("작업A", 1));
maxHeap.offer(new Task("작업B", 2));
System.out.println(maxHeap.poll()); //작업C(3) - 우선순위 가장 높음
/* Comparable도 Comparator도 없으면 */
class Job {
    String name;
    //Comparable 구현 안 함
}
PriorityQueue<Job> queue = new PriorityQueue<>();
queue.offer(new Job("Job1")); //런타임 에러
//ClassCastException
//해결: Comparator 제공
PriorityQueue<Job> queue = new PriorityQueue<>(
    (j1, j2) -> j1.name.compareTo(j2.name)
);
```
- HashSet, HashMap
- 정렬 불필요, Comparable/Comparator 불필요
- 정렬하려면 List로 변환 후 정렬
- (2) Comparable, Comparator
```
Comparable
- 패키지: java.lang
- 메서드: compareTo(T o)
- 정의 위치: 클래스 내부
- 정렬 기준: 1개 (기본 정렬)
- 사용 시점: 클래스 설계 시
Comparator
- 패키지: java.util
- 메서드: compare(T o1, T o2)
- 정의 위치: 클래스 외부
- 정렬 기준: 여러 개 가능
- 사용 시점: 런타임에 유연하게
```
- Comparable - 기본 정렬 순서
```java
public interface Comparable<T> {
    int compareTo(T o);
}
//클래스 자체에 정의하는 자연스러운 순서
//단 하나의 정렬 기준만 제공
//클래스를 수정할 수 있을 때 사용
```
- compareTo 반환값 규칙
```java
//this와 other를 비교
int compareTo(T other) {
    //음수: this < other (this가 앞으로)
    //0: this == other (같음)
    //양수: this > other (this가 뒤로)
}
```
- 예시
```java
/* 예시 1: 숫자 정렬 */
class Student implements Comparable<Student> {
    String name;
    int score;
    public Student(String name, int score) {
        this.name = name;
        this.score = score;
    }
    @Override
    public int compareTo(Student other) {
        //점수 기준 오름차순 (낮은 점수가 앞으로)
        return this.score - other.score;
        //내림차순이면: return other.score - this.score;
    }
    @Override
    public String toString() {
        return name + "(" + score + ")";
    }
}
//사용
List<Student> students = new ArrayList<>();
students.add(new Student("Alice", 85));
students.add(new Student("Bob", 92));
students.add(new Student("Charlie", 78));
Collections.sort(students);  //compareTo() 자동 사용
//결과: [Charlie(78), Alice(85), Bob(92)]
/* 예시 2: 문자열 정렬 */
class Person implements Comparable<Person> {
    String name;
    int age;
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    @Override
    public int compareTo(Person other) {
        //이름 기준 사전순
        return this.name.compareTo(other.name);
    }
}
/* 예시 3: 다중 조건 정렬 */
class Employee implements Comparable<Employee> {
    String department;
    String name;
    int salary;
    @Override
    public int compareTo(Employee other) {
        //1순위: 부서명 사전순
        int deptCompare = this.department.compareTo(other.department);
        if (deptCompare != 0) return deptCompare;
        //2순위: 급여 내림차순
        int salaryCompare = other.salary - this.salary;
        if (salaryCompare != 0) return salaryCompare;
        //3순위: 이름 사전순
        return this.name.compareTo(other.name);
    }
}
/* Java 기본 클래스들의 Comparable */
//Integer: 숫자 크기순
Integer a = 10, b = 20;
a.compareTo(b); //-1 (a < b)
//String: 사전순
String s1 = "apple", s2 = "banana";
s1.compareTo(s2); //음수 (a < b)
//Date: 시간순
Date d1 = new Date(2024, 1, 1);
Date d2 = new Date(2024, 12, 31);
d1.compareTo(d2); //음수 (d1 < d2)
```
- Comparator - 다양한 정렬 기준
```java
@FunctionalInterface
public interface Comparator<T> {
    int compare(T o1, T o2);
}
//클래스 외부에서 정의
//여러 정렬 기준 동시에 가능
//클래스를 수정할 수 없을 때 사용
//전략 패턴(Strategy Pattern) 구현
```
- compare 반환값 규칙
```java
int compare(T o1, T o2) {
    //음수: o1 < o2 (o1이 앞으로)
    //0: o1 == o2 (같음)
    //양수: o1 > o2 (o1이 뒤로)
}
```
- 예시
```java
/* 예시 1: 익명 클래스 */
class Student {
    String name;
    int score;
    public Student(String name, int score) {
        this.name = name;
        this.score = score;
    }
}
List<Student> students = new ArrayList<>();
students.add(new Student("Alice", 85));
students.add(new Student("Bob", 92));
students.add(new Student("Charlie", 78));
//점수 기준 오름차순
Collections.sort(students, new Comparator<Student>() {
    @Override
    public int compare(Student s1, Student s2) {
        return s1.score - s2.score;
    }
});
//점수 기준 내림차순
Collections.sort(students, new Comparator<Student>() {
    @Override
    public int compare(Student s1, Student s2) {
        return s2.score - s1.score;
    }
});
//이름 기준 사전순
Collections.sort(students, new Comparator<Student>() {
    @Override
    public int compare(Student s1, Student s2) {
        return s1.name.compareTo(s2.name);
    }
});
/* 예시 2: 람다 표현식 (Java 8+) */
//점수 오름차순
Collections.sort(students, (s1, s2) -> s1.score - s2.score);
//점수 내림차순
Collections.sort(students, (s1, s2) -> s2.score - s1.score);
//이름 사전순
Collections.sort(students, (s1, s2) -> s1.name.compareTo(s2.name));
//List.sort() 사용
students.sort((s1, s2) -> s1.score - s2.score);
/* 예시 3: Comparator 정적 메서드 (Java 8+) */
//comparing() - 가장 많이 사용
students.sort(Comparator.comparing(s -> s.score));
students.sort(Comparator.comparing(s -> s.name));
//메서드 참조
students.sort(Comparator.comparing(Student::getScore));
//reversed() - 역순
students.sort(Comparator.comparing(Student::getScore).reversed());
//thenComparing() - 다중 정렬
students.sort(
    Comparator.comparing(Student::getDepartment)
        .thenComparing(Student::getScore)
        .thenComparing(Student::getName)
);
//nullsFirst(), nullsLast()
students.sort(Comparator.nullsFirst(
    Comparator.comparing(Student::getScore)
));
//naturalOrder(), reverseOrder()
List<Integer> numbers = Arrays.asList(5, 2, 8, 1);
numbers.sort(Comparator.naturalOrder()); //[1, 2, 5, 8]
numbers.sort(Comparator.reverseOrder()); //[8, 5, 2, 1]
```
- 혼합 예시
```java
class Book implements Comparable<Book> {
    String title;
    int year;
    public Book(String title, int year) {
        this.title = title;
        this.year = year;
    }
    @Override
    public int compareTo(Book other) {
        //기본 정렬: 출판년도 순
        return this.year - other.year;
    }
    @Override
    public String toString() {
        return title + "(" + year + ")";
    }
}
//사용
List<Book> books = new ArrayList<>();
books.add(new Book("Java Guide", 2020));
books.add(new Book("Python Basics", 2018));
books.add(new Book("C++ Advanced", 2022));
Collections.sort(books); //compareTo() 사용
//결과: [Python Basics(2018), Java Guide(2020), C++ Advanced(2022)]
//제목 기준으로 정렬
Comparator<Book> titleComparator = (b1, b2) -> b1.title.compareTo(b2.title);
Collections.sort(books, titleComparator);
//결과: [C++ Advanced(2022), Java Guide(2020), Python Basics(2018)]
//출판년도 역순으로 정렬
Comparator<Book> yearDescComparator = (b1, b2) -> b2.year - b1.year;
Collections.sort(books, yearDescComparator);
//결과: [C++ Advanced(2022), Java Guide(2020), Python Basics(2018)]
```
- Comparable 사용 시점
- 클래스를 직접 만들 때, 명확한 자연스러운 순서가 있을 때
- 단일 정렬 기준으로 충분할 때, TreeSet, TreeMap에서 자동 정렬하고 싶을 때
- Comparator 사용 시점
- 클래스를 수정할 수 없을 때
```java
//String 클래스를 수정할 수 없음
List<String> names = Arrays.asList("John", "alice", "Bob");
//대소문자 무시 정렬
names.sort(String.CASE_INSENSITIVE_ORDER);
```
- 여러 정렬 기준이 필요할 때
```java
students.sort((s1, s2) -> s1.score - s2.score); //점수순
students.sort((s1, s2) -> s1.name.compareTo(s2.name)); //이름순
students.sort((s1, s2) -> s1.age - s2.age); //나이순
```
- 런타임에 정렬 기준을 선택하고 싶을 때
```java
Scanner sc = new Scanner(System.in);
System.out.println("정렬 기준: 1.점수 2.이름");
int choice = sc.nextInt();
if (choice == 1) {
    students.sort(Comparator.comparing(Student::getScore));
} else {
    students.sort(Comparator.comparing(Student::getName));
}
```
- 역순 정렬이 필요할 때
```java
students.sort(Comparator.comparing(Student::getScore).reversed());
```
- null 처리가 필요할 때
```java
students.sort(Comparator.nullsFirst(
    Comparator.comparing(Student::getScore)
));
```
- 정리
```
상황 - 사용 - 예시
클래스 설계 시 기본 정렬 - Comparable - class User implements Comparable<User>
여러 정렬 기준 - Comparator - 점수순, 이름순, 날짜순 등
클래스 수정 불가 - Comparator - String, Integer 등
TreeSet/TreeMap 자동 정렬 - Comparable - TreeSet<User>
런타임에 정렬 변경 - Comparator - 사용자 선택에 따라
역순 정렬 - Comparator - .reversed()
복합 정렬 - Comparator - .thenComparing()
```
- (3) Dual-Pivot Quicksort
- Java의 Arrays.sort(int[])에서 사용되는 알고리즘
- 일반 Quicksort(퀵정렬)은 피벗(pivot) 하나를 기준으로 데이터를 두 부분으로 나눈다.
- Dual-Pivot Quicksort는 이름처럼 피벗을 두 개 사용
- 배열 [9, 4, 7, 3, 8, 2, 6, 1, 5] 가 있다.
- step 1. 두 개의 피벗을 선택 (예: p1=3, p2=7)
- step 2. 세 구역으로 나눔:
- p1보다 작은 값들/p1~p2 사이 값들/p2보다 큰 값들
- 각 구역을 다시 재귀적으로 정렬
- 피벗이 하나일 때보다 분할이 더 잘 되면 정렬이 더 빠를 수 있다.
- Dual-Pivot Quicksort는 불안정한 정렬
- A(3), B(3), C(2)을 정렬할 때, 값이 같은 A와 B의 원래 순서가 바뀔 수 있다.
- 퀵정렬이 요소를 교환(swap)하며 제자리(in-place) 정렬을 하기 때문
- 즉, 동일한 값의 상대적 순서를 보장하지 않는다.
- 장점: 빠르고 메모리 효율적 (in-place)
- 단점: 불안정, 최악의 경우 느림 O(n^2)
- (3) TimSort
- Python의 sort(), Java의 Arrays.sort(Object[])에서 사용됨
- Merge Sort + Insertion Sort를 섞은 하이브리드 정렬
- step 1. 배열에서 이미 부분적으로 정렬된 구간(run)을 찾음
- 예: [2,3,5], [7,6,4] 이런 식으로 이미 정렬된 작은 구간들
- 이 구간들을 병합 정렬(Merge Sort) 방식으로 합친다.
- 작은 구간(run)은 삽입 정렬(Insertion Sort)로 빠르게 정리
- 실제 데이터가 어느 정도 정렬되어 있는 경우 효율적
- TimSort는 안정적인 정렬
- A(3), B(3), C(2)을 정렬하면, 결과는 항상 C(2), A(3), B(3)
- 같은 값(3)인 A와 B의 원래 순서가 그대로 유지
- 병합 정렬 기반이라 원래 순서를 보존하며 병합을 수행해서 그렇다.
- 장점: 안정적, 실제 데이터에서 매우 빠름, 실무에서 많이 사용
- 단점: 구현이 복잡하고, 약간의 추가 메모리 필요
- (4) Insetion Sort
- 손으로 카드 정렬하는 방식과 거의 같다.
- 하나씩 꺼내서, 이미 정렬된 부분에 알맞은 위치에 삽입
- 예를 들어 [5, 2, 4, 6] 정렬할 때:
- step 1. 5 → 첫 원소는 그냥 둠
- step 2. 2 → 5보다 작으니 앞으로 삽입 → [2, 5, 4, 6]
- step 3. 4 → 5 뒤에 삽입 → [2, 4, 5, 6]
- step 4. 6 → 이미 맞는 자리에 있음
- 결과: [2, 4, 5, 6]
- 평균/최악: O(n^2)
- 최선: O(n) (이미 정렬되어 있는 경우)
- 안정적 - 같은 값을 가진 원소의 순서가 바뀌지 않음
- 작고 단순한 문제에 강함
- (5) Merge Sort
- 나누고 정복하라(Divide and Conquer)
- step 1. 배열을 반으로 나눔
- step 2. 각각을 재귀적으로 정렬
- step 3. 두 정렬된 배열을 병합(merge)해서 하나로 만듦
- 예: [5, 2, 4, 6]
```css
[5, 2, 4, 6]
→ [5, 2] + [4, 6]
→ [2, 5] + [4, 6]
→ 병합 → [2, 4, 5, 6]
```
- 항상 O(nlogn) = 분할(logn) × 병합(n)
- 안정적 - 병합 시 순서를 유지
- 추가 메모리 공간 필요 (임시 배열)
- 항상 일정하고 안정적
- (6) Quick Sort
- step 1. 하나의 피벗(pivot) 선택
- step 2. 피벗보다 작은 값/큰 값으로 분할
- step 3. 각각을 재귀적으로 정렬
- 예: [5, 2, 4, 6, 1, 3]
```css
피벗 = 4
→ 작은 값: [2, 1, 3]
→ 큰 값: [5, 6]
→ 각 부분 정렬 후 합침 → [1, 2, 3, 4, 5, 6]
```
- 평균: O(nlogn)
- 최악: O(n^2) (피벗이 계속 한쪽에만 치우칠 때)
- 불안정 - 정렬 중에 swap(교환)으로 순서가 바뀔 수 있음
- 메모리 효율적 (in-place)
- 빠름 (특히 무작위 데이터에서), 피벗 선택이 중요
- 대부분의 경우 가장 빠름 (하지만 불안정)