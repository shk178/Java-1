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
- 다이아몬드 연산자(<>) 덕분에 생성자에서 타입을 생략할 수 있다.
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
- 타입 매개변수: GenBox<T>의 T
```
- E = Element
- K = Key
- N = Number
- T = Type
- V = Value
- S, U, V = 2nd, 3rd, 4th types
예) class Data<K, V> {}
```
- 타입 인자: GenBox<Integer>의 Integer
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
- <Animal> 이라고 제네릭 타입 매개변수를 선언했다.
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
- <T>는 이 메서드가 제네릭을 쓴다는 것을 선언하는 부분이다.
- <T>는 리턴 타입 앞에 와야 한다.
- 3. 제네릭 메서드 선언부 구조
```java
public static <T> T genMethod(T t) {}
//[접근제어자] [static] <타입매개변수들> [리턴타입] 메서드이름(매개변수...) { ... }
//static 안 써도 된다. (인스턴스 메서드도 된다.)
```
- <타입매개변수들>은 그 메서드 안에서만 유효한 타입 변수 선언부다.
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
- 제네릭 클래스(GenericClass<T>)는 객체 생성 시 타입 인자 전달한다.
- 제네릭 메서드(<T> T GenericMethod(T t))는 호출 시 타입 인자 전달한다.
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
- (5) 메서드의 <T>는 메서드 호출 시점에 독립적으로 결정된다.
- shadowing 현상: 메서드의 <T>가 클래스의 <T>를 가린다고 한다.
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
- 독자적인 (이름이 같든 다르든) 제네릭 타입 <T> T를 선언해야 한다.