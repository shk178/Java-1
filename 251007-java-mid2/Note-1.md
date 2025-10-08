# 2. 제네릭 - Generic1
- 1. ObjBox 만들기 (다형성을 통한 중복 해결 시도)
```java
public class ObjBox {
    private Object value;
    public void set(Object obj) {
        this.value = obj;
    }
    public Object get() {
        return value;
    }
    public static void main(String[] args) {
        ObjBox obj1 = new ObjBox();
        obj1.set(10); //int -> Integer로 오토 박싱
        Object getobj1 = obj1.get();
        System.out.println(getobj1.getClass()); //class java.lang.Integer
        //Integer one = getobj1; //java: incompatible types: java.lang.Object cannot be converted to java.lang.Integer
        Integer one = (Integer) getobj1; //단축키 Inline Variable (Ctrl+Alt+N) 하면 getobj1 -> obj1.get()으로 치환
    }
}
```
- int는 클래스가 아니고 객체가 아님
- Object 타입에는 객체만 저장 가능
- Integer로 명시적 다운캐스팅 후 사용
```java
        obj1.set("100"); //숫자 100 쓰려고 했는데, 문자열로 "100" 썼을 때
        Integer two = (Integer) obj1.get(); //Exception in thread "main" java.lang.ClassCastException: 런타임 오류 발생
```
- 제약이 없다 보니까 실수할 가능성 = 타입 안정성이 낮다.
- 2. GenBox
```java
//클래스명은 GenBox
//클래스 종류는 제네릭 클래스 (타입을 미리 결정하지 않음)
//제네릭 클래스는 클래스명 옆에 <T>와 같이 선언한다.
//T를 타입 매개변수라고 한다.
public class GenBox<T> {
    private T value;
    public void set(T t) {
        this.value = t;
    }
    public T get() {
        return value;
    }
    public static void main(String[] args) {
        GenBox<Integer> gen1 = new GenBox<Integer>(); //생성 시점에 타입 결정
        //gen1.set("100"); //java: incompatible types: java.lang.String cannot be converted to java.lang.Integer
        gen1.set(10);
        System.out.println(gen1.get().getClass()); //class java.lang.Integer
    }
}
```
- 제네릭 클래스는 객체 생성 시점에 타입을 지정할 수 있다.
- 컴파일러가 지정한 타입인지 확인해서 타입 안정성이 높다.
- 3. 제네릭 타입 추론(Generic Type Inference)
```java
        GenBox<Integer> gen2 = new GenBox<>(); //생성 시점에 타입 추론
```
- 컴파일러가 생성자 호출 시점에 타입을 유추
- 다이아몬드 연산자(`<>`) 덕분에 생성자에서 타입을 생략할 수 있다.
- 컴파일러는 변수 선언부의 타입을 보고 (= 왼쪽) 추론한다.
- 타입을 추론할 수 있는 정보가 없거나 모호할 때는 오류가 발생
```java
GenBox<?> gen = new GenBox<>();
//<?>는 와일드카드라서 타입을 정확히 알 수 없다.
//와일드카드는 set() 메서드도 사용할 수 없다.
var gen = new GenBox<>();
//이 경우에도 타입 추론은 생성자에 명시된 타입이 있어야 정확하다.
//new GenBox<>()만 쓰면 GenBox<Object>로 추론된다.
```
- 4. 제네릭 용어와 관례
- 메서드는 매개변수에 인자 전달해 사용할 값 결정
- 제네릭 클래스는 타입 매개변수에 타입 인자 전달해 사용할 타입 결정
- 제네릭 클래스, 제네릭 인터페이스를 제네릭 타입이라고 한다.
- 타입 매개변수: `GenBox<T>`의 T
```
- E = Element
- K = Key
- N = Number
- T = Type
- V = Value
- S, U, V = 2nd, 3rd, 4th types
예) class Data<K, V> {}
```
- 타입 인자: `GenBox<Integer>`의 Integer
- 타입 인자로 기본형 사용x, 기본형 대신 래퍼 클래스 쓴다.
- 5. Raw 타입
- Raw Type은 제네릭이 도입되기 이전 방식
- 타입 파라미터를 명시하지 않고 제네릭 클래스를 사용한다.
```java
public class RawType {
    public static void main(String[] args) {
        GenBox gen3 = new GenBox(); //Raw Type - Object로 추론
        GenBox<Integer> gen4 = new GenBox<>(); //권장되는 방식
        gen3.set(10);
        System.out.println(gen3.get().getClass()); //class java.lang.Integer
        //Integer i = gen3.get(); //java: incompatible types: java.lang.Object cannot be converted to java.lang.Integer
    }
}
```
- 타입 안정성이 보장되지 않는다.
- 런타임 오류가 발생할 수 있다.
# 3. 제네릭 - Generic2
- 1. 제네릭 타입에 상한(extends) 지정
```java
public class AniHos<Animal> {
    private Animal animal;
    //...
}
```
- `<Animal>` 이라고 제네릭 타입 매개변수를 선언했다.
- Animal 클래스가 아니라 Animal이라는 새로운 타입 매개변수(T)를 만든 거다.
- getName() 등 제네릭 클래스 코드에 쓸 수 없다.
```java
public class AniHos<T extends Animal> {
    private T animal;
    public void set(T animal) {
        this.animal = animal;
    }
    public void checkUp() {
        System.out.println(animal.getName());
        System.out.println(animal.getSize());
        animal.sound();
    }
    public T bigger(T target) {
        return animal.getSize() > target.getSize() ? animal : target;
    }
}
```
- 상한을 지정하면, 타입 매개변수가 가지는 타입의 최대 상위 클래스를 제한한다.
- 제네릭 타입 T가 반드시 Animal의 하위 클래스여야 한다는 뜻이 된다.
- T target도 T animal과 같은 타입만 쓸 수 있다.
```java
public class AniMain2 {
    public static void main(String[] args) {
        AniHos<Dog> dogHospital = new AniHos<>();
        dogHospital.set(new Dog("멍멍이", 10));
        Animal dog1 = dogHospital.bigger(new Dog("누렁이", 20));
        System.out.println(dog1); //Animal{name='누렁이', size=20}
        AniHos<Cat> catHospital = new AniHos<>();
        catHospital.set(new Cat("야옹이", 5));
        //dogHospital.set(new Cat("야옹이", 5));
        //Animal dog2 = dogHospital.bigger(new Cat("야옹이", 5));
        //java: incompatible types: generic.Cat cannot be converted to generic.Dog
    }
}
```
- 2. 제네릭 메서드
```java
public class GenMethod {
    //일반 메서드
    public static Object objMethod(Object obj) {
        System.out.println(obj);
        return obj;
    }
    //제네릭 메서드
    public static <T> T genMethod(T t) {
        System.out.println(t);
        return t;
    }
}
String s = "Hello";
String result = (String) GenMethod.objMethod(s); //Object로 받음
//결과 타입이 Object이므로 형변환 필요
String s = "Hello";
String result = GenMethod.genMethod(s); //<T>가 String으로 추론됨
//결과 타입도 String이므로 형변환 불필요
```
- 제네릭 메서드는 매개변수 타입과 리턴 타입이 호출 시 결정된다.
- obj 메서드는 형변환이 필요하고 타입 안정성 낮다.
- gen 메서드는 형변환 불필요하고 타입 안정성 높다.
```java
Integer i = GenMethod.genMethod(123); //T → Integer
Double d = GenMethod.genMethod(3.14); //T → Double
Boolean b = GenMethod.genMethod(true); //T → Boolean
//호출 시점에 타입이 자동으로 정해진다.
```
- `<T>`는 이 메서드가 제네릭을 쓴다는 것을 선언하는 부분이다.
- `<T>`는 리턴 타입 앞에 와야 한다.
- 3. 제네릭 메서드 선언부 구조
```java
public static <T> T genMethod(T t) {}
//[접근제어자] [static] <타입매개변수들> [리턴타입] 메서드이름(매개변수...) { ... }
//static 안 써도 된다. (인스턴스 메서드도 된다.)
```
- `<타입매개변수들>`은 그 메서드 안에서만 유효한 타입 변수 선언부다.
- 여러 개도 쓸 수 있고, 서로 다른 제약도 걸 수 있다.
- (1) 타입 두 개 선언
```java
public static <T, S> T genMethod(S s) {
    System.out.println("입력: " + s);
    T result = null;
    return result;
}
//<T, S> → 제네릭 타입 두 개 선언
//매개변수 S s : S 타입의 값
//반환값 T : T 타입의 값
//입력 타입(S)과 출력 타입(T)이 서로 다를 수 있다.
```
- (2) 타입 관계 있는 버전
```java
public static <T extends Number, S extends T> T genMethod(S s) {
    System.out.println(s);
    return s; //S는 T의 하위 타입이니까 반환 가능
}
//두 타입 사이에 제약(상한)도 걸 수 있다.
//S는 T의 하위 타입이어야 한다는 의미
Number n = genMethod(123); //S = Integer, T = Number
Double d = genMethod(3.14); //S = Double, T = Number
```
- S를 제네릭을 쓴다는 것을 선언하지 않으면 틀린 코드다.
```java
public static <T> T genMethod(S s) {} //컴파일 에러
//컴파일러가 S가 어디서 왔는지 모른다.
```
- 4. 제네릭 클래스와 제네릭 메서드
- 제네릭 클래스(`GenericClass<T>`)는 객체 생성 시 타입 인자 전달한다.
- 제네릭 메서드(`<T> T GenericMethod(T t)`)는 호출 시 타입 인자 전달한다.
- (1) 제네릭 클래스의 타입 파라미터
- 인스턴스 필드/메서드에서 사용 가능하다.
```java
class Box<T> {
    private T value; //가능
    public void setValue(T value) { //가능
        this.value = value;
    }
}
```
- static 필드/메서드에서 사용 불가하다.
```java
class Box<T> {
    static T staticValue; //컴파일 에러
    static void staticMethod(T param) { //컴파일 에러
    }
}
//static 멤버는 클래스가 로드될 때 메모리에 할당되는데
//타입 파라미터는 객체가 생성될 때 결정되기 때문
```
- (2) 제네릭 메서드의 타입 파라미터
- 일반 메서드로 사용 가능하다.
```java
class Util {
    public <T> void printArray(T[] array) { //가능
        for (T element : array) {
            System.out.println(element);
        }
    }
}
```
- static 메서드로 사용 가능하다.
```java
class Util {
    static <T> void swap(T[] array, int i, int j) { //가능
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
//제네릭 메서드의 타입 파라미터는 메서드 호출 시점에 결정되므로
//static 메서드에서도 사용 가능하다.
```
- (3) 제네릭 클래스와 메서드는 독립적인 타입 파라미터를 가질 수 있다.
```java
class Box<T> {
    private T value;
    //제네릭 클래스의 T와 별개의 제네릭 메서드
    public <U> void printPair(U other) { //가능
        System.out.println("Box: " + value);
        System.out.println("Other: " + other);
    }
    //클래스의 T를 사용하는 인스턴스 메서드
    public void setValue(T value) {
        this.value = value;
    }
}
//제네릭 클래스의 T는 객체 생성 시점에 결정
//제네릭 메서드의 <U>는 메서드 호출 시점에 결정
//같은 메서드 내에서 클래스의 T와 메서드의 <U>를 모두 사용 가능
Box<String> box = new Box<>();
box.setValue("Hello");
box.printPair(123); //U는 Integer로 추론됨
```
- (4) 제네릭 클래스에서 제네릭 메서드를 static으로 사용할 수 있다.
```java
class Box<T> {
    private T value;
    //가능 - static 제네릭 메서드
    static <U> void printValue(U value) {
        System.out.println("Value: " + value);
    }
}
Box.printValue("Hello"); //U는 String
Box.printValue(123); //U는 Integer
```
- (5) 메서드의 `<T>`는 메서드 호출 시점에 독립적으로 결정된다.
- shadowing 현상: 메서드의 `<T>`가 클래스의 `<T>`를 가린다고 한다.
```java
public class ComplexBox2<T extends Animal> { //클래스의 T
    private T animal; //이 T는 main에서 Dog으로 고정됨
    public <T> T printAndReturn(T t) { //메서드의 T는 클래스의 T와 무관
        System.out.println("animal: " + animal.getClass()); //Dog
        System.out.println("t: " + t.getClass()); //메서드의 T
        return t;
    }
}
ComplexBox2<Dog> box = new ComplexBox2<>(); //클래스의 T = Dog
box.set(dog);
box.printAndReturn(cat); //메서드의 <T> = 메서드 호출 시 Cat으로 결정됨
//명확하게 하려면 서로 다른 이름을 사용하는 게 낫다.
```
- (6) 제네릭 클래스가 static 제네릭 메서드 사용할 때
```java
class Box<T> {
    static <T> void method(T param) {
        System.out.println(param);
    }
}
//class Box<T>의 T → 클래스 수준의 타입 매개변수
//Box<String>, Box<Integer> 처럼 인스턴스를 만들 때 구체화된다.
//static <T> void method(T param)의 T → 메서드 수준의 타입 매개변수
//이건 메서드를 호출할 때 별도로 추론되며, 클래스의 T와 아무 관련이 없다.
//두 T는 이름이 같을 뿐, 서로 다른 타입 변수다.
```
- static은 클래스 로딩 시점에 이미 메모리에 올라가는 요소다.
- 클래스의 T는 인스턴스를 만들 때 구체화되는 정보다.
```java
Box<String> box1 = new Box<>();
Box<Integer> box2 = new Box<>();
//이렇게 해도 Box.class는 딱 한 번만 로딩된다.
//JVM 입장에서는 Box<String>과 Box<Integer>가 같은 클래스(Box)다.
```
- 제네릭은 컴파일러 수준에서만 체크되고, 런타임에는 타입 소거(type erasure)되어 없어진다.
- 그래서 static 영역(클래스 레벨)에선 어떤 T인지 알 수 없음 → 클래스의 T를 쓸 수 없음이다.
```java
class Box<T> {
    static void method(T param) { } //오류: static context에서 T를 참조할 수 없음
}
```
- 클래스의 제네릭 타입 매개변수는 인스턴스에 속한다.
- static 메서드는 클래스에 속한다.
- 따라서 static 메서드는 클래스의 제네릭 타입 매개변수를 사용할 수 없다.
- 독자적인 제네릭 타입 `<T> T`를 선언해야 한다.
- 가능한 한 클래스랑 메서드의 타입 매개변수를 서로 다른 이름으로 한다.
- 5. 제네릭 메서드 우선순위 > 제네릭 클래스 우선순위
- 제네릭 클래스에 타입 상한 적용해도, 제네릭 메서드에 적용되지 않는다.
```java
public class ComplexBox3<T extends Animal> {
    static <Z> Z printAndReturn(Z z) {
        System.out.println("z: " + z.getClass());
        //z.sound();
        //java: cannot find symbol
        //  symbol:   method sound()
        //  location: variable z of type Z
        return z;
    }
    public static void main(String[] args) {
        Dog dog = new Dog("누렁이", 100);
        ComplexBox3.printAndReturn(dog);
    }
}
```
- 제네릭 메서드에도 상한을 적용한다.
```java
public class ComplexBox3<T extends Animal> {
    static <Z extends Animal> Z printAndReturn(Z z) {
        System.out.println("z: " + z.getClass());
        z.sound();
        return z;
    }
    public static void main(String[] args) {
        Dog dog = new Dog("누렁이", 100);
        ComplexBox3.printAndReturn(dog);
        //z: class generic.Dog
        //멍
    }
}
```
- 6. 와일드카드
- 프로그래밍에서 와일드카드는 임의의 값이나 타입을 대표하는 기호
- (1) 자바 제네릭에서의 와일드카드
- 제네릭 타입을 다룰 때 ?를 사용해 와일드카드를 표현
- 제네릭 타입 간의 상속 관계를 유연하게 처리
- 컬렉션(List, Set 등)을 다룰 때 유용
- `<?>` (Unbounded Wildcard)
- 어떤 타입이든 허용
- 읽기 가능, 쓰기 불가
- 어떤 타입인지 모르기 때문에 안전하게 읽을 수만 있음
- `<? extends T>` (상한 제한 와일드카드)
- T와 그 하위 타입만 허용
- 읽기 가능, 쓰기 불가
- T의 하위 타입이지만 정확한 타입을 몰라서 쓰기 불가능
- `<? super T>` (하한 제한 와일드카드)
- T와 그 상위 타입만 허용
- 쓰기 가능, 읽기 제한적
- T의 상위 타입이므로 T나 그 하위 타입을 안전하게 쓸 수 있음
- (2) 와일드카드 문자
- 파일 시스템, 검색, 정규표현식 등에서 문자열 패턴 매칭에 사용
- `*`: 길이가 0 이상인 임의의 문자열을 의미
- 예: `doc*` → `doc`, `document` 등과 일치
- `?`: 임의의 한 문자
- 예: `123?` → `1234`, `1235` 등과 일치
- (3) 와일드카드 읽기/쓰기 제한
- 읽기/쓰기 제한은 타입 안정성을 위한 자바의 보호 장치
```
Producer Extends, Consumer Super 원칙
- 데이터를 생산(읽기)할 때는 extends
- 데이터를 소비(쓰기)할 때는 super
```
- `<?>` (Unbounded Wildcard, 비제한 와일드카드)
- 모든 타입을 허용하므로, 타입이 무엇인지 알 수 없다.
- `List<?>`는 `List<String>`, `List<Integer>` 등 어떤 것이든 될 수 있다.
- 읽기는 가능하지만, 쓰기(추가)는 불가
```java
List<?> list = new ArrayList<String>();
Object obj = list.get(0); //읽기는 가능
list.add("hello"); //컴파일 에러: 타입을 알 수 없기 때문
```
- `<? extends T>` (상한 제한 와일드카드)
- T의 하위 타입만 허용
- `<? extends Number>`는 Integer, Double 등이 될 수 있다.
- 하지만 정확히 어떤 하위 타입인지 알 수 없기 때문에 쓰기가 불가
```java
List<? extends Number> numbers = new ArrayList<Integer>();
Number n = numbers.get(0); //읽기는 가능
numbers.add(3.14); //컴파일 에러: 정확한 타입을 몰라서
```
- `<? super T>` (하한 제한 와일드카드)
- T의 상위 타입만 허용
- `<? super Integer>`는 Number, Object 등이 될 수 있다.
- 그래서 Integer나 그 하위 타입을 안전하게 추가할 수 있다.
- 하지만 읽을 때는 Object로만 읽을 수 있다: 정확한 타입을 알 수 없기 때문
```java
List<? super Integer> list = new ArrayList<Number>();
list.add(10); //쓰기는 가능
Object obj = list.get(0); //읽기는 가능하지만 타입은 Object
```
- (4) `<? super T>` 와일드카드
- list는 Integer의 상위 타입을 받는다. (`List<Number>`, `List<Object>` 등)
```java
List<? super Integer> list = new ArrayList<Number>();
```
- list가 Integer의 상위 타입을 타입 파라미터로 가진 리스트를 참조할 수 있다는 뜻
- `new ArrayList<Number>()` = 실제로 `List<? super Integer>`에 할당된 객체
- ArrayList implements List: ArrayList는 List의 기능을 실제로 구현한 클래스
```
//자바 컬렉션 계층 구조
Collection (인터페이스)
 └── List (인터페이스) //순서 있는 요소 집합 정의 인터페이스
      ├── ArrayList (클래스) //배열 기반 구현 클래스
      ├── LinkedList (클래스) //링크드 리스트 기반 구현 클래스
      └── Vector (클래스)
//자바에서 하위 타입: 자식 클래스, 인터페이스 구현 클래스
//ArrayList는 List의 하위 타입이므로
//List 타입 변수에 ArrayList 객체를 담을 수 있다.
//다형성(polymorphism) - 와일드카드 상한/하한과는 별개다.
```
- list.add(...)에 넣을 수 있는 건 Integer 또는 그 하위 타입이다.
```java
List<? super Integer> list = new ArrayList<Number>();
list.add(new Integer(10)); //가능
        list.add(5); //가능 (int → Integer 자동 박싱)
list.add(new Object()); //불가능 (Object는 Integer보다 상위지만, 타입 보장 안 됨)
```
- (5) `<? extends T>` 와일드카드
- `List<? extends Number>`는 `List<Integer>`, `List<Double>` 등일 수 있다.
- list.add(...)를 하려고 하면 컴파일러는 Integer인지, Double인지 모른다.
- 추가하려는 값이 안전한지 확신할 수 없다.
- 읽기는 가능하다.
- 이 리스트는 Number 또는 그 하위 타입을 담고 있다.
- 정확히 어떤 하위 타입인지는 모르지만, 최소한 Number라는 건 보장
- Number 클래스의 메서드만 바로 사용할 수 있다.
```java
List<? extends Number> list = new ArrayList<Integer>();
list.add(3.14); //에러: Double일 수도 Float일 수도..
list.add(42); //에러: Integer도 안 됨
Number n = list.get(0); //가능
```
- Integer나 Double의 고유 메서드를 사용하려면 명시적으로 캐스팅
- 캐스팅 시 런타임 오류 주의한다.
- 7. 제네릭 타입/메서드와 와일드카드 비교
- (1) 제네릭 타입/메서드
- 타입 매개변수 T를 명시적으로 선언하고, 사용할 때 구체적인 타입을 지정
```java
class Box<T> {
    T value;
    void set(T value) { this.value = value; }
    T get() { return value; }
}
Box<Integer> intBox = new Box<>(); //타입을 명시적으로 지정
```
- 장점: 타입이 명확해서 읽기/쓰기 모두 자유롭고 타입 안정성 높음
- 단점: 타입을 고정해야 하므로 유연성이 떨어질 수 있음
- (2) 와일드카드
- 타입을 `?`로 표현해서 불특정한 제네릭 타입을 받아들일 수 있음
```java
void printList(List<?> list) {
    for (Object item : list) {
        System.out.println(item);
    }
}
```
- 장점: 다양한 제네릭 타입을 한 번에 처리 가능
- 단점: 타입이 불확정이라서 읽기/쓰기 제약이 생김
- (3) 클래스/메서드 정의에서는 와일드카드 사용 불가
- 클래스/메서드 정의에는 제네릭 타입/메서드 사용한다.
```java
class Box<?> { //컴파일 에러
    ? value; //컴파일 에러
}
//?는 타입을 모호하게 표현하는 용도
//클래스나 메서드 정의에서는 구체적인 타입 매개변수가 필요
```
- (4) 와일드카드 사용 예
- 와일드카드는 이미 만들어진 제네릭 타입을 활용할 때 사용된다.
```java
class Box<T> {
    T value;
    void set(T value) { this.value = value; }
    T get() { return value; }
}
Box<? extends Number> box = new Box<Integer>(); //가능
//Box를 사용할 때, 타입을 모호하게 열어두는 방식
//Box는 T로 정의되어 있고
//사용하는 쪽에서 ? extends Number처럼 와일드카드를 지정할 수 있다.
```
- 8. 타입 이레이저(Type Erasure)
- 제네릭은 컴파일 시에만 존재하고 (.java)
- 실행 시에는 사라지는 특징 (.class)
```java
//List<T>는 제네릭 인터페이스
//T는 타입 매개변수
public interface List<T> {
    void add(T element);
    T get(int index);
    // ...
}
List<String> list = new ArrayList<>();
list.add("hello");
String str = list.get(0);
```
- 컴파일 전: 코드에서 `List<String>`처럼 String이라고 타입을 구체화했다.
- 컴파일 후에는 이 정보가 바이트코드에서 사라져서 그냥 `List`로 처리된다.
- 자바는 후방 호환성(backward compatibility)을 중요하게 생각
- 제네릭이 도입된 건 자바 5부터
- 이전 버전의 JVM에서 실행되도록 제네릭 정보를 런타임에 남기지 않는다.
- 컴파일 중: `List<T>`에서 T를 String으로 치환해서 타입 체크를 수행
- 컴파일 후
```java
//컴파일이 후 자바는 타입 정보를 제거
//즉, T는 Object로 대체되거나
//제한된 타입(bound)이 있으면 그 타입으로 바뀜
//타입 이레이저가 발생했다고 한다.
public interface List {
    void add(Object element);
    Object get(int index);
}
List<String> list = new ArrayList<>();
list.add("hello");
String str = list.get(0);
```
- 코드는 컴파일 시에는 타입 체크가 되지만
- 런타임에는 그냥 Object로 처리
- 그래서 list.get(0)은 내부적으로 Object를 반환
- 컴파일러가 자동으로 String으로 캐스팅해준다.
- 타입 이레이저의 영향
- (1) 런타임에 타입 정보 알 수 없다.
- (2) 리플렉션으로도 제네릭 타입을 알 수 없다.
- (3) 컴파일러가 막아주지만, 잘못된 캐스팅은 런타임 오류로 이어질 수 있음
- 251007-java-mid2/src/generic2/ErBox.java
- T가 실행 시점에 Object로 바뀌니까 instanceof가 항상 참이고
- 항상 new Object()가 실행되니까 자바 컴파일러가 오류 낸다.
- 251007-java-mid2/src/generic2/ErBox2.java
- 9. 연습문제
- 251007-java-mid2/src/generic2/ex/UnitUtil.java
- (1) this.getClass()
- 런타임 시점의 실제 타입을 반환
- 변수의 선언 타입이 아니라 실제 생성된 객체 타입을 기준으로 한다.
- 다형성(polymorphism)으로 되는 거다.
- (2) maxHp() 메서드
```java
private static <T extends BioUnit> T maxHp(T t1, T t2) {
    return t1.getHp() > t2.getHp() ? t1 : t2;
}
//바이트코드에서 타입 소거
private static BioUnit maxHp(BioUnit t1, BioUnit t2)
```
```java
Marine m1 = new Marine("마린1", 40);
Marine m2 = new Marine("마린2", 50);
Marine r = UnitUtil.maxHp(m1, m2);
//바이트코드에 캐스팅이 존재
Marine r = (Marine) UnitUtil.maxHp((BioUnit) m1, (BioUnit) m2);
```
- (3) get 메서드
```java
private static Marine getMarine(BioUnit unit) {
    return (Marine) unit;
}
private static Zealot getZealot(Zealot z) {
    return z;
}
private static BioUnit getZergling(Zergling z) {
    return z;
}
//매개변수 타입, 반환 타입 맞게 업캐스팅, 다운캐스팅 다 된다.
//메서드 - 매개변수 타입 - 반환 타입
//getMarine - BioUnit - Marine
//getZealot - Zealot - Zealot
//getZergling - Zergling - BioUnit
```
- `Box<T>`는 제네릭 클래스
- `Box<? extends Animal>`를 인자로 받는 메서드가 있음
- 메서드 안에서 box.get()을 호출함
- box.get()의 반환 타입 = Animal
```java
//Box<? extends Animal>는 Animal을 상속한 타입을 담는 박스
//하지만 메서드 안에서는 그 구체적인 타입이 뭔지 알 수 없다.
//Dog일 수도 있고 Cat일 수도 있고, 그냥 Animal일 수도 있다.
//그래서 가장 안전한 공통 상위 타입인 Animal로 추론한다.
Box<? extends Animal> box = ...;
Animal a = box.get(); //가능
Dog d = box.get(); //컴파일 오류
//? extends Animal은 읽기 전용처럼 동작
//어떤 Animal의 서브타입인지 모르기 때문에, 값을 넣는 건 불가
box.set(new Dog()); //컴파일 오류
```
- `Box<? super Animal>`이면 box.set(new Dog()) 가능
- box.get()은 Object 타입으로 반환