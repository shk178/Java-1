# 2. Object 클래스
- java.object 패키지 - 자바에서 자동으로 import되는 패키지
- Object 클래스: 모든 자바 클래스의 최상위 부모
- public class MyClass { } //자동으로 extends Object가 생략되어 있음
- MyClass obj = new MyClass();
- obj.toString(); //객체를 문자열로
- obj.equals(other); //객체 비교
- obj.hashCode(); //해시코드 반환
- obj.getClass(); //클래스 정보 반환
- String 클래스: 문자열을 다루는 클래스, 불변(immutable) 객체
- Wrapper 타입: 기본형(primitive)을 객체로 감싸는 클래스
- Integer num = 10; //래퍼 타입 (오토박싱)
- 컬렉션에는 객체만 저장 가능하다: List<Integer> (○), List<int> (×)
- null 값을 표현해야 할 때
- 유틸리티 메서드 사용 위해
- Class 클래스: 클래스의 메타 정보(설계도의 정보)를 담는 클래스
- public class Person { private String name; public void hello() {} }
- Class<Person> clazz1 = Person.class; //Class 객체 얻기
- String className = clazz1.getName(); //Class 정보 조회 "Person"
- 메서드, 필드 정보 조회 (리플렉션)
- Person person = clazz1.getDeclaredConstructor().newInstance(); //객체 생성
- System 클래스: 시스템 레벨의 기능을 제공하는 유틸리티 클래스
- 표준 입출력, 현재 시간 (밀리초), 성능 측정용 시간 (나노초)
- String javaVersion = System.getProperty("java.version"); //시스템 속성
- System.arraycopy(srcArr, 0, destArr, 0, 3); //배열 빠른 복사
- System.gc(); System.exit(0); //가비지 컬렉션 제안, 프로그램 종료
- soutm 단축키: 클래스명.메서드명 출력
- "extends 클래스"가 없으면 묵시적으로 Object 클래스를 상속받는다. (최상위 부모 Object)
- 1. Child child = new Child(); Child child2 = new Child(); Parent poly = child;
- 2. System.out.println(child.toString()); //object.Child@4e50df2e
- 3. System.out.println(child.equals(child2)); //false
- 4. System.out.println(child.equals(poly)); //true
- 5. System.out.println(child.hashCode()); //1313922862
- 6. System.out.println(child.getClass()); //class object.Child
- new Object();로 인스턴스 만들 수 있다.
- Object는 모든 객체를 다 참조할 수 (담을 수) 있다. 다형성 지원하는 기본 메커니즘 제공
- 하지만 Object로 업캐스팅 했을 때, 자식 클래스의 메서드 호출할 수 없다. (오버라이딩이 아님)
- 1. Object obj = new Dog();
- 2. if (obj instanceof Dog dog1) { dog1.sound(); }
- obj instanceof Dog가 true일 때 Dog dog1 = (Dog) obj;를 실행한다. (패턴 매칭 기능)
- 3. if (!(obj instanceof Dog dog2)) { return; } dog2.sound();
- 블록에 return/throw/continue/break가 있으면 확정적이어서 컴파일러가 블록 밖에서 접근을 허용한다.
- 4. while (obj instanceof Dog dog3 && dog3.isHungry()) { dog3.eat(); }
- 5. switch (obj) { case Dog dog4 -> dog4.sound(); }
- 컴파일러는 구문 분석, 의미 분석, 지역 변수 슬롯 계산 (모든 경로 분석), 바이트코드 생성한다.
- JVM이 .class 파일 읽고 타입 안정성/스택 오버플로우 체크하고 메서드 영역에 클래스 정보 로딩한다.
- new Dog();하면 힙 메모리에 Dog 객체 생성된다: 메서드 포인터, 필드
- 메서드 호출 시 JVM 스택에 스택 프레임이 생긴다: 지역 변수 테이블(슬롯), 오퍼랜드 스택, 프레임 데이터
- 바이트코드가 실행된다. instanceof가 true이면 dog1 슬롯에 (Dog) obj 값이 할당된다.
- if문 블록을 빠져나가면 여전히 스택 프레임 슬롯에 참조값이 할당되어 있지만, 컴파일러가 접근 제한한다.
- 슬롯이 가능한 지역 변수 개수만큼 생성되는 건 아니고 최적화로 공유되기도 한다. (다른 타입이어도 덮어씀)
- 패턴 매칭이 있지만, Object를 다형성으로 활용하기에는 메서드 오버라이딩에 한계가 있다.
- Object가 유용한 경우가 있다: 프레임워크/라이브러리 내부에서..
- Object 배열을 사용하거나, Object 매개변수/반환 타입, Object를 담는 자료구조, 내부 구현에서 사용 등
- generate 단축키: alt + insert (toString() 오버라이딩)
- toString(), hashCode()에서 사용하는 참조값 얻기: System.identityHashCode(obj) 실행
- 정적 의존은 컴파일 타임에 결정되는 의존 관계로, 코드에 타입이 명시되어 있다.
- 동적 의존은 런타임에 결정되는 의존 관계로, 전달될 객체가 실행 시점에 결정된다.
- class A가 메서드에서 (Car car)를 사용하는 경우 A는 Car에 의존한다고 표현한다.
- 구체적인 것에 의존하는 경우다. Car를 Bus로 바꾸려면 코드를 변경한다.
- -> A는 Car에 정적 의존하고, Car의 하위 클래스에 동적 의존할 가능성 있다.
- class B가 메서드에서 (Animal animal)를 사용하는 경우 B는 Animal에 의존한다고 표현한다.
- 추상적인 것(추상 클래스, 인터페이스)에 의존하는 경우다. Dog든 Cat이든 쓸 수 있다.
- -> B는 Animal에 정적 의존하고, Dog나 Cat에 동적 의존할 가능성 있다.
- class C가 메서드에서 (Object o)를 사용하는 경우 C는 Object에 의존한다고 표현한다.
- 너무 추상적이라서 활용도는 낮다. Object 메서드를 오버라이딩한다면 활용도 높다.
- -> C는 Object 클래스에 정적 의존하고, 모든 클래스에 동적 의존할 가능성 있다.
- Object 대 인터페이스: 다형적 참조 - 오브젝트는 모든 타입을 > 인터페이스는 구현 클래스를 받는다.
- Object 대 인터페이스: 오버라이딩 - 오브젝트는 오브젝트 메서드만 < 인터페이스는 정의된 모든 메서드
- Object 대 인터페이스: 타입 체크 - 오브젝트는 instanceof 필요 < 인터페이스는 불필요
- Object 대 인터페이스: 컴파일 안정성 - 오브젝트는 런타임 오류 가능 < 인터페이스는 컴파일 타임 체크
- 1. class PowerOutlet { public void plug(Object d) { Phone p = (Phone) d; p.charge(); } }
- 2. outlet.plug(new Phone()); //ok
- 3. outlet.plug(new Laptop()); //런타임 ClassCastException
- 1. interface Chargeable { void charge(); }
- 2. class Phone implements Chargeable { public void charge() { } }
- 3. class Laptop implements Chargeable { public void charge() { } }
- 4. class PowerOutlet { public void plug(Chargeable device) { device.charge(); } }
- 5. outlet.plug(new Phone()); //ok
- 6. outlet.plug(new Laptop()); //ok
- 7. outlet.plug(new Book()); //컴파일 오류
- 자바 객체가 같다 - 동일성 (Identity), 동등성 (Equality)
- 동일성이란, 물리적 동등성: 두 참조 변수가 메모리상 같은 객체를 가리키는지 == 연산자로 비교한다.
- 1. String a = new String("hello");
- 2. String b = new String("hello");
- 3. String c = a;
- 4. System.out.println(a == b); //false (서로 다른 객체)
- 5. System.out.println(a == c); //true (같은 객체를 참조)
- 동등성이란, 논리적 동등성: 두 객체의 내용이나 값이 같은지 equals() 메서드로 비교한다.
- 1. String a = new String("hello");
- 2. String b = new String("hello");
- 3. System.out.println(a.equals(b)); //true
- 커스텀 클래스를 만들 때는 equals()를 오버라이딩해서 논리적 동등성을 정의한다.
- 1. Person p1 = new Person("김철수", 25); Person p2 = new Person("김철수", 25);
- 2. System.out.println(p1.equals(p2)); //false
- Object 기본 구현에서, equals()는 this == obj를 반환하기 때문이다.
- String, Integer, ArrayList 등 표준 클래스에서 동등성 목적으로 오버라이딩 해둔 거였다.
- 1. @Override public boolean equals(Object o) {
- 2. if (this == o) return true;
- 3. if (o == null || this.getClass() != o.getClass()) return false;
- 4. Person p = (Person) o; //필드 접근 위해 명시적 다운캐스팅
- 5. return this.age == p.age && this.name.equals(p.name); }
- 이렇게 하면 p1.equals(p2)가 true다.
- equals() 생성 단축키: generator (alt + insert)
- 1. @Override public int hashCode() {
- 2. return Objects.hash(name, age); //같은 값이면 같은 해시코드 }
- equals()와 hashCode()는 컬렉션에서 함께 사용된다. hashCode()도 오버라이딩 필요해진다.
- Objects.equals()는 import java.util.Objects; 해야 쓸 수 있다. Objects는 유틸리티 클래스이다.
- Object에 clone(), 멀티쓰레드용 메서드 등이 더 있다.
---
# 3. 불변 객체
- 기본형: 하나의 값을 변수들이 공유하지 않는다.
- 참조형: 참조값을 통해 하나의 객체를 변수들이 공유한다.
- 사이드 이펙트: 주된 작업 외에 추과적인 효과를 일으키는 것. 이로 인해 디버깅 어렵고, 안정성 저하될 가능성o
- 참조값을 항상 다르게 하면 객체 공유x, 참조형의 사이드 이펙트 해결됨
- 하지만 객체 공유가 문법 오류x, 막을 방법이 없다. 객체 공유의 장점도 있다.
- 객체 공유를 막지 말고, 객체의 값을 항상 바꿀 수 없도록 하자.
- 불변 객체: 객체의 상태(내부 값=필드=멤버 변수)가 변하지 않는 객체
- 불변 클래스를 만드는 규칙 1. 클래스를 final로 선언해 상속을 방지
- 규칙 2. 모든 필드를 private final로 선언해 외부 접근을 막고 재할당을 방지
- 규칙 3. setter 메서드를 제공하지 않아 필드 값을 변경할 수 없게 함
- 규칙 4. 생성자를 통해서만 초기화 가능하도록 함
- 규칙 5. 가변 객체 필드는 방어적 복사를 통해 외부에서 변경되지 않도록 함
- 불변 객체의 장점 1. 여러 스레드가 동시에 접근해도 안전
- 장점 2. 상태가 변하지 않아 캐싱으로 안전하게 재사용 가능
- 장점 3. HashMap 등의 키로 사용해도 안전
- 장점 4. 상태가 변하지 않아 디버깅이 쉬움
- 불변 객체 예시: String, Integer, LocalDate
- 규칙 5. 추가 설명: 복사본을 주고받아서 원본을 보호한다는 의미다.
- List나 Array 같은 가변 객체는 참조값을 전달하기 때문에, 외부에서 그 내용을 바꿀 수 있다.
```java
//잘못된 예시
public final class Person {
    private final List<String> hobbies;
    public Person(List<String> hobbies) {
        this.hobbies = hobbies; //그냥 참조만 저장
    }
    public List<String> getHobbies() {
        return hobbies; //그냥 참조만 반환
    }
}
List<String> myHobbies = new ArrayList<>();
myHobbies.add("독서");
Person person = new Person(myHobbies);
myHobbies.add("게임"); //Person의 hobbies도 변경됨
person.getHobbies().add("운동"); //또 변경됨
```
- 외부와 연결을 끊기 위해 새로운 리스트를 만들어서 복사한다. (방어적 복사)
```java
public final class Person {
    private final List<String> hobbies;
    public Person(List<String> hobbies) {
        this.hobbies = new ArrayList<>(hobbies); //새 리스트를 만들어 복사
    }
    public List<String> getHobbies() {
        return new ArrayList<>(hobbies); //새 리스트를 만들어 복사
    }
}
//외부에서 리스트를 변경해도 Person의 hobbies는 영향을 받지 않는다.
```
- with: (new 불변객체생성자)를 반환하는 메서드를 withDay와 같이 이름짓는다.
# 4. String 클래스
- println() 출력할 타입 확인: ctrl + p 단축키
- String은 참조형이다.
- 문자열 풀: 같은 내용의 문자열을 메모리에 1번 저장하고 재사용
- 문자열 풀은 힙 영역에 있다. GC로 관리되고, 해시 알고리즘으로 빠르게 찾는다.
```java
String str1 = "hello";
String str2 = "hello";
System.out.println(str1 == str2); //true
System.out.println(str1.equals(str2)); //true
```
- new를 사용하면 같은 내용의 문자열이어도 새로운 객체 만든다.
```java
String str3 = new String("hello");
String str4 = new String("hello");
System.out.println(str3 == str4); //false
System.out.println(str3.equals(str4)); //true
```
- String은 불변이다.
```java
String str = "hello";
str.toUpperCase(); //"HELLO", 객체 생성해 반환
System.out.println(str); //"hello"
```
- 문자열을 계속 이어붙이는 등 자주 바꾸면 성능 저하된다. (참조형이라 + 할 수 없지만 지원한다.)
- 문자열 가변 클래스를 사용한다. (원본 수정) - StringBuilder, StringBuffer
- StringBuilder: 빠르다, 단일 스레드에 사용한다.
- StringBuffer: 느리다, 멀티 스레드에 사용한다. (안전)
- String에서 문자 데이터는 byte[]에 보관된다.
```java
public final class String {
    private final byte[] value;
    private final byte coder;
    public int length() {...}
    ... //String - ctrl + b 하면 나온다.
}
```
- 원래 char[]였는데 byte[]로 바꾸고, coder에 인코딩 정보를 함께 저장하게 됐다.
- coder 필드 = 0: LATIN1, 1바이트로 충분한 영어, 숫자, 기본 기호로 구성된 경우
- coder 필드 = 1: UTF16, 2바이트가 필요한 한글, 이모지 등이 포함된 경우
- String 클래스 주요 메서드
```java
String str = "hello";
str.length(); //5 (문자열 길이)
str.isEmpty(); //false (빈 문자열"" 인지)
str.isBlank(); //false (공백만"  " 있는지)
str.charAt(0); //'h' (특정 위치의 문자)
//str.charAt(-1); //IndexOutOfBoundsException!
str.toCharArray(); //['h','e','l','l','o'] (char 배열로 변환)
str.concat(" world"); //"hello world" (문자열 연결)
str.matches("[a-z]+"); //true (정규식 매칭)
String str2 = "hello world";
str2.substring(0, 5); //"hello" ([0] ~ [4])
str2.substring(6); //"world" ([6] ~)
// 다중 라인 문자열
String text = """
        여러 줄
        문자열입니다
        """;
```
- 위치 검색, 포함 여부, 대소문자 변환, 공백 처리, 교체, 분리, 결합, 비교, 형변환, 반복, 포맷팅 등 있다.
- 스택 영역: 지역 변수 (기본형 실제 값), 매개변수, 참조 변수 (객체의 주소), 메서드 호출 정보 (리턴 주소 등)
- 스택 영역: 메서드가 끝나면 자동 제거, 빠른 접근 속도, 1MB 정도로 크기 작음, 스레드별 독립
- 힙 영역: 모든 객체, 인스턴스 변수 (객체의 필드), 배열, 문자열 풀
- 힙 영역: GC가 관리, 크기가 큼 (동적으로 조절 가능), 모든 스레드가 공유
- Metaspace: 클래스 메타데이터 (클래스 구조 정보), 메서드 정보 (바이트코드), static 변수, 상수 풀 (런타임 상수), final 상수
- Metaspace: JVM 힙 메모리가 아닌 OS의 네이티브 메모리 사용, 크기 제한이 없고 유연하게 확장, 동적 클래스 로딩에 유리
- Metaspace: 프로그램 종료 시 제거, 모든 스레드가 공유
- 스택 영역: 컴파일러가 스택 프레임에 몇 개의 슬롯이 필요한지 locals 값을 미리 계산한다. 스코프가 다르면 재사용하도록 한다.
- 슬롯 - 지역 변수 테이블에서 변수를 저장하는 단위: 1슬롯 = 4바이트, int/float/참조변수 = 1슬롯, long/double = 2슬롯
- 스택 영역은 메모리 할당을 컴파일 타임에 결정한다.
```java
public void method(int a, int b) {
    int c = 10;
    String d = "hello";
}
//컴파일러: 매개변수 2개 (a, b), 지역변수 2개 (c, d) → 확정
//컴파일러: 총 4개 + this = 5슬롯 필요 -> locals = 5 바이트코드에 기록 -> .class 생성
```
- 힙 영역과 Metaspace는 메모리 할당을 런타임에 결정한다.
```java
public void createStudents() {
    Scanner sc = new Scanner(System.in);
    int count = sc.nextInt(); //사용자가 입력
    for (int i = 0; i < count; i++) {
        new Student(); //몇 개 만들지 컴파일 시점엔 모름
    }
} //컵파일러: 힙 영역에 객체를 얼마나 만들지 모르니까
public void loadClasses(String[] classNames) {
    for (String name : classNames) {
        Class.forName(name); //동적으로 클래스 로딩
    }
} //컴파일러: Metaspace에 클래스를 얼마나 로딩할지 모르니까
```
- 스택 영역, 힙 영역, Metaspace 모두 실제 메모리 할당은 런타임에 JVM이 수행한다.
```java
//컴파일 타임: "2개 슬롯 필요!" (계획)
public void example() {
    int a = 10; //← 이 순간 JVM이 스택에 할당
    int b = 20; //← 이 순간 JVM이 스택에 할당
}
//컴파일 타임: "Person 객체 생성 코드가 있네" (계획 X, 단지 인지만)
public void create() {
    Person p = new Person(); //← 이 순간 JVM이 힙에 할당
}
//컴파일 타임: .class 파일 생성
public class MyClass {
    static int count = 0;
} //런타임: 클래스 로딩 시점에 JVM이 Metaspace에 할당
```
- 클래스 로딩 시 Metaspace에 할당되는 것들: [Example 클래스 영역]
```java
public class Example {
    static final String NAME = "Java"; //(5)
    static int count = 0; //(3)
    public void greet() { //(2), (4)
        System.out.println("Hello");
    }
} //(1)
```
```
(1) 클래스 메타데이터
- 클래스명: Example
- 필드: NAME (static final String), count (static int)
- 메서드: greet()
- 접근제어자, 상속 정보 등
(2) 메서드 정보
- greet() 메서드의 바이트코드
(3) static 변수
- count = 0 (실제 값 저장)
(4) 상수 풀
- "Hello" 문자열 참조
- System 클래스 심볼 참조
- println 메서드 심볼 참조
(5) final 상수
- NAME = "Java" (실제 값 또는 참조)
```
- 1. static final String NAME = "Java"; -> 컴파일 타임 상수
- static final 기본형/String - 컴파일 시점에 확정되어서 바이트코드에 직접 삽입됨
- 상수 풀에 저장됨, NAME = "Java"
- 별도의 메모리 공간 불필요 (NAME이 "Java"로 치환됨)
- 2. static final int[] ARRAY = {1, 2, 3}; -> 런타임 상수
- static final 객체 - 런타임에 객체가 생성
- 생성 시점: 클래스 로딩 시 (프로그램 시작 시 한 번)
- 저장 위치: 실제 객체 → 힙, 참조(주소) → Metaspace의 static 영역
- 힙에 객체가 만들어지고, 0xABCD: int[] {1, 2, 3}
- 그 참조값이 static 영역에 저장됨, ARRAY → 0xABCD
- 3. final int value; //static 아닌 final -> 런타임 상수
- final (non-static) - 런타임에 객체가 생성
- 생성 시점: 인스턴스 생성 시마다
- 저장 위치: 실제 객체 → 힙, 참조(주소) → 힙의 각 Config 객체 내부
- 1. trim() - 전통적인 방식 - ASCII 공백만 제거 (스페이스, 탭, 줄바꿈)
- 스페이스 (U+0020), 탭 (\t), 줄바꿈 (\n), 캐리지 리턴 (\r), 기타 ASCII 제어 문자 (U+0000 ~ U+0020)
- 2. strip() - 현대적인 방식 - ASCII 공백 + 유니코드 공백 모두 제거
- 유니코드 공백 (모든 공백 문자), 중국어/일본어 공백 (U+3000), Non-breaking space (U+00A0)
- 문자열 변경 동안 StringBuilder를 사용하다가 변경 후에 String으로 고정한다.
- 자바 컴파일러는 문자열 리터럴의 +를 자동 처리해준다.
```java
//컴파일 전
String helloWorld = "Hello, " + "World";
//컴파일 후
String helloWorld = "Hello, World";
//컴파일 전
String result = str1 + str2;
//컴파일 최적화
String result = new StringBuilder().append(str1).append(str2).toString();
//자바 9부터는 StringConcatFactory로 최적화를 수행한다.
```
- 반복문에서는 컴파일러가 예측이 어려워서, new StringBuilder()가 루프마다 실행된다.
- 반복문, 조건문, 대용량 등 복잡한 상황에서는 직접 StringBuilder를 활용하자.
- 메서드 체이닝 (Method Chaining) - 객체 지향 프로그래밍에서 여러 메서드를 연속적으로 호출할 수 있게 하는 기법
- 한 메서드의 반환값으로 자기 자신(this)이나 또 다른 객체를 반환해서, 그 객체에 이어서 메서드를 호출할 수 있도록 함
- 코드를 간결하고 읽기 쉽게 만들 수 있지만, 디버깅이 어렵다.
- StringBuilder는 메서드 체이닝 기법을 제공한다.
- String result = String.join("-", "a", "b", "c"); //"a-b-c"
- join()은 static 메서드다. String 객체 이름으로는 호출이 안 된다..
```java
//static: 여러 문자열을 처리, 특정 대상 없음
String.join("-", "a", "b"); //여러 문자열을 합침
String.valueOf(123); //숫자를 문자열로
String.format("%s %d", "age", 20); //포맷팅
```
- join()에 배열을 쓸 수 있는 이유 - 가변 인자(Varargs)
```java
public static String join(CharSequence delimiter, CharSequence... elements)
//                                                  ^^^ 가변 인자 (...)
//... 표시는 가변 인자(varargs)로, 개별 문자열로 or 배열로 or List로 전달 다 된다.
//elements는 내부적으로 배열로 처리됨
```
- StringBuilder 결과를 String에 저장하려면 .toString();으로 변환한다.
---