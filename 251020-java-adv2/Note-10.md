# 13. 리플렉션
- 클래스의 메타데이터: 클래스명, 접근 제어자, 부모 클래스, 구현한 인터페이스 등
- 필드 정보: 필드명, 타입, 접근 제어자, 필드 값 접근
- 메서드 정보: 메서드명, 반환 타입, 매개변수 정보, 메서드 호출
- 생성자 정보: 매개변수 정보, 객체 생성
- getFields()
    - public 필드만 반환합니다.
    - 상속받은 public 필드도 포함됩니다.
    - 즉, 접근 제어자가 public이 아닌 필드는 조회되지 않아요.
- getDeclaredFields()
    - 해당 클래스에 선언된 모든 필드를 반환합니다.
    - 접근 제어자와 관계없이 private, protected, default, public 모두 포함됩니다.
    - 단, 상속받은 필드는 포함되지 않아요.
- getMethods()
    - public 메서드만 반환합니다.
    - 상속받은 public 메서드도 포함됩니다.
- getDeclaredMethods()
    - 해당 클래스에 선언된 모든 메서드를 반환합니다.
    - 상속받은 메서드는 포함되지 않아요.
- getConstructors()
    - public 생성자만 반환합니다.
    - 상속 개념은 없지만, 외부에서 접근 가능한 생성자만 조회돼요.
- getDeclaredConstructors()
    - 해당 클래스에 선언된 모든 생성자를 반환합니다.
- `Field[] allFields = basicDataClass.getDeclaredFields();` // 클래스 내부에 선언된 모든 필드 조회
- `Method[] allMethods = basicDataClass.getDeclaredMethods();` // 클래스 내부에 선언된 모든 메서드 조회
- `Constructor<?>[] allConstructors = basicDataClass.getDeclaredConstructors();` // 전체 생성자 조회
- `field.setAccessible(true);` // private 필드 접근 허용 후 사용 가능
- `method.setAccessible(true);` // private 메서드 접근 허용 후 사용 가능
```java
Constructor<?> constructor = basicDataClass.getDeclaredConstructor();
constructor.setAccessible(true); // 접근 허용
Object instance = constructor.newInstance(); // 인스턴스 생성
```
## Modifier
- Java에서 Modifier(수식자)는 클래스, 메서드, 필드(멤버 변수) 등이 가진 접근 제어자나 기타 속성을 나타내는 메타데이터 정보
- Modifier는 클래스나 멤버의 속성을 설명하는 정보
- 예를 들어 클래스나 메서드 선언부에 붙는 public, private, static, final, abstract 같은 키워드들이 modifier
```java
public abstract class Example {
    private static final int VALUE = 10;
    public final void doSomething() {}
}
// Example 클래스의 수식자 	public, abstract
// VALUE 필드의 수식자 	private, static, final
// doSomething() 메서드의 수식자 	public, final
```
## 리플렉션(Reflection)에서의 Modifier
- java.lang.reflect.Modifier 클래스는 이런 수식자 정보를 비트 플래그 형태로 관리
```java
Class<?> clazz = Example.class;
int mod = clazz.getModifiers();
System.out.println(Modifier.isPublic(mod)); // true
System.out.println(Modifier.isAbstract(mod)); // true
// Example 클래스가 public abstract인 걸 프로그램적으로 알 수 있다.
// Modifier.toString()으로 출력도 된다.
```
- 주요 Modifier 종류	키워드	설명
- 접근 제어자	public, protected, private	접근 범위를 결정
- 클래스/메서드 관련	abstract, final, static, synchronized, native, strictfp	동작 방식 제어
- 필드 관련	volatile, transient	JVM의 메모리 관리나 직렬화 관련 제어
- 기타	interface, enum, annotation 등	타입 선언 관련
- 접근 제어 변경은 신중하게 한다.
- 리플렉션 활용하면 다양한 객체에 발생하는 공통 문제 처리할 수 있다.
```
String s1 = null; // 아무것도 없음
String s2 = ""; // 비어 있는 문자열 객체 (메모리에 있다.)
System.out.println(s1.length()); // NullPointerException
System.out.println(s2.length()); // 0 출력
int a = 0; // 항상 값이 있어야 함
Integer b = null; // 객체 참조이므로 null 가능
Integer num = null; int val = num; // NullPointerException (자동 언박싱)
```
- 251020-java-adv2/src/http4 // ReflectorController, ReflectionServlet
# 14. 애노테이션
- 주석은 컴파일 타임에 사라지지만, 애노테이션은 안 사라진다.
- 애노테이션은 프로그램 코드가 아니고 주석의 일종이다.
- 커스텀 애너테이션(Custom Annotation)을 정의
- `@Retention(RetentionPolicy.RUNTIME)`
- @Retention은 이 애너테이션이 언제까지 유지될지를 지정하는 메타 애너테이션
- RetentionPolicy.RUNTIME은 이 애너테이션이 런타임까지 유지된다는 뜻
- 즉, 리플렉션(Reflection)을 통해 런타임 중에 이 애너테이션 정보를 읽을 수 있다.
- 예: 프레임워크나 라이브러리에서 이 애너테이션을 보고 동작을 바꾸는 경우
- `public @interface SimpleMapping`
- @interface는 애너테이션을 정의하는 키워드
- SimpleMapping이라는 이름의 애너테이션을 새로 만든다.
- public은 접근 제어자로, 다른 패키지에서도 이 애너테이션을 사용할 수 있게 한다.
- 꼭 public이어야 하는 건 아니지만, 다른 클래스나 패키지에서 사용할 목적이라면 public이 필요
- default 접근 제어자면 같은 패키지 내에서만 사용 가능
- `String value();`
- 이건 애너테이션의 속성(attribute)을 정의한 것
- value라는 이름의 속성을 만들고, 타입은 String
- 사용 예:
```java
@SimpleMapping("hello")
// 이렇게 쓰면 value 속성에 "hello"가 들어간다.
// 속성이 하나이고 이름이 value일 때는 이름 없이 값만 써도 된다.
```
```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    String path();
    String method();
}
@Route(path="/home", method="GET")
// 속성이 여러 개면 이름=값으로 써야 된다.
```
- 왜 괄호를 쓰는 걸까
- 애너테이션은 내부적으로 속성(=메서드처럼 생긴 정의)을 가지고 있고, 괄호 안에 그 속성의 값을 지정하는 방식
```java
public @interface Info {
    String author();
    int version();
}
// author()와 version()은 사실 메서드처럼 정의된 속성이지만, 값을 설정하는 용도로만 쓰인다.
// 괄호는 그 값을 전달하는 문법적 장치일 뿐, 실제로 메서드를 호출하는 건 아니다.
// @Annotation(...)은 마치 속성 있는 태그를 다는 느낌이다.
// HTML에서 <div class="highlight">처럼 속성을 지정하듯이
// Java에서는 @Highlight(color="red")처럼 괄호 안에 속성을 넣는다.
```
- 애노테이션 정의 규칙
- 데이터 타입: 기본 타입, String, Class, 인터페이스, enum, 다른 애노테이션, 앞서 나열한 타입들의 배열
- 요소에 default 값을 지정할 수 있다.
- 요소는 메서드 형태로 정의되는데 괄호가 있지만 매개변수는 없다.
- void를 반환 타입으로 사용할 수 없다.
- 예외를 선언할 수 없다.
```java
@Retention(RetentionPolicy.RUNTIME) // 런타임에도 유지되어 리플렉션(reflection)으로 읽을 수 있다.
public @interface AnnoElement {
    String value(); // 필수 문자열 값
    int count() default 0; // 기본값이 0인 정수
    String[] tags() default {}; // 기본값이 빈 배열인 문자열 배열
    Class<? extends MyLogger> annoData() default MyLogger.class; // MyLogger 클래스를 상속한 클래스 타입 (기본값은 MyLogger.class)
}

// ElementData 클래스에 @AnnoElement 애너테이션을 붙였다.
@AnnoElement(value = "data", count = 10, tags = {"t1, t2"})
public class ElementData {
}

public static void main(String[] args) {
    Class<ElementData> annoClass = ElementData.class; // ElementData.class를 통해 클래스 객체를 얻는다.
    AnnoElement annotation = annoClass.getAnnotation(AnnoElement.class);
    // getAnnotation()을 통해 AnnoElement 애너테이션 정보를 가져온다.
    // 런타임에 애너테이션 값을 읽으려면 getAnnotation() 또는 유사한 리플렉션 API를 사용해야 한다.
    System.out.println(annotation.value()); // data
    System.out.println(annotation.count()); // 10
    System.out.println(Arrays.toString(annotation.tags())); // [t1, t2]
    System.out.println(annotation.annoData()); // class network.MyLogger
}
```
- 메타 애너테이션
- 애너테이션을 정의할 때 사용하는 애너테이션
- 커스텀 애너테이션(@interface)을 만들 때 그 애너테이션이 언제, 어디에, 어떻게 적용될지를 설정
- `@Target`: 애너테이션을 적용할 수 있는 대상(클래스, 메서드, 필드 등)을 지정
```
애너테이션을 적용할 수 있는 위치를 제한 (컴파일 시점에 위치 체크한다.)
| enum 상수            | 의미                         |
| ------------------ | -------------------------- |
| `TYPE`             | 클래스, 인터페이스, enum, record   |
| `FIELD`            | 필드 (멤버 변수, enum 상수)        |
| `METHOD`           | 메서드                        |
| `PARAMETER`        | 메서드의 매개변수                  |
| `CONSTRUCTOR`      | 생성자                        |
| `LOCAL_VARIABLE`   | 지역 변수                      |
| `ANNOTATION_TYPE`  | 애너테이션 자체                   |
| `PACKAGE`          | 패키지 선언부                    |
| `TYPE_PARAMETER`   | 제네릭 타입 매개변수                |
| `TYPE_USE`         | 타입이 사용되는 모든 위치 (Java 8 이후) |
| `MODULE`           | 모듈 선언 (Java 9 이후)          |
| `RECORD_COMPONENT` | record의 컴포넌트 (Java 16 이후)  |
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface MyAnnotation {} // MyAnnotation은 메서드나 필드에만 붙일 수 있다.
```
- `@Retention`: 애너테이션이 얼마 동안 유지될지를 지정 (소스, 클래스, 런타임 중 선택)
```
애너테이션이 언제까지 유지될지를 지정
| enum 상수   | 의미                           |
| --------- | ---------------------------- |
| `SOURCE`  | 소스 코드에만 존재 (컴파일 시 제거됨)       |
| `CLASS`   | 클래스 파일(.class)까지 존재 (기본값)    |
| `RUNTIME` | 런타임 시에도 JVM에 남아 리플렉션으로 참조 가능 |
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {} // 런타임에도 리플렉션으로 정보를 읽을 수 있다.
```
- `@Documented`: Javadoc 등 문서화 도구에서 애너테이션 정보를 포함할지 지정
```
Javadoc으로 문서를 생성할 때, 해당 애너테이션이 붙은 요소의 문서에 애너테이션 내용도 포함
@Documented
public @interface MyAnnotation {} // API 문서에 @MyAnnotation이 표시된다.
```
- `@Inherited`: 부모 클래스의 애너테이션이 자식 클래스에 상속될지 지정
```
애너테이션이 상속 관계에서 자식 클래스에도 자동으로 적용될지 결정 (클래스에만 적용)
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {}
@MyAnnotation
class Parent {}
class Child extends Parent {} // Child도 @MyAnnotation을 “상속받은 것처럼” 보임
// 진짜 상속이 아니라 리플렉션 시 상속된 것처럼 보이게 하는 효과
// 즉, 컴파일러가 실제로 Child 클래스에 애너테이션을 붙여주는 건 아니다.
public static void main(String[] args) {
    System.out.println(Parent.class.isAnnotationPresent(MyAnnotation.class)); // true
    System.out.println(Child.class.isAnnotationPresent(MyAnnotation.class)); // true
}
// Child.class에는 직접적으로 @MyAnnotation이 붙어있지 않습니다.
// 하지만 @Inherited 덕분에 리플렉션(isAnnotationPresent)으로 조회할 때
// 부모 클래스의 애너테이션이 자동으로 검색되어 반환됩니다.
// 이건 클래스(Class)에만 적용됩니다. 메서드, 필드, 생성자에는 상속되지 않아요.
class Parent {
    @MyAnnotation
    void hello() {}
}
class Child extends Parent {
    void hello() {} // 부모 메서드의 애너테이션은 상속 안 됨
}
```
- `@Repeatable`: 같은 애너테이션을 여러 번 반복해서 사용할 수 있도록 지정
```
하나의 요소에 같은 애너테이션을 여러 번 붙일 수 있도록 한다. (컨테이너 애너테이션을 함께 정의해야 한다.)
@Repeatable(MyAnnotations.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {
    String value();
}
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotations {
    MyAnnotation[] value();
}
@MyAnnotation("A")
@MyAnnotation("B")
public class TestClass {}
// 자바 컴파일러는 내부적으로 다음과 같이 처리
@MyAnnotations({@MyAnnotation("A"), @MyAnnotation("B")})
class TestClass {}
```
```
@interface Role {
    String value();
}
@Role("Admin")
@Role("User") // Duplicate annotation (Role은 한 번만 사용할 수 있다는 컴파일 오류)
class Member {}
// 해결책: @Repeatable + 컨테이너 애너테이션
@Repeatable(Roles.class) // 이 애너테이션은 Roles라는 컨테이너에 담겨서 여러 개를 저장할 수 있다
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface Role {
    String value();
}
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@interface Roles {
    Role[] value();
}
@Role("Admin")
@Role("User")
class Member {}
// 자바 컴파일러는 내부적으로 다음처럼 자동 변환
@Roles({
    @Role("Admin"),
    @Role("User")
})
class Member {}
즉, “컨테이너 애너테이션”이란
같은 종류의 애너테이션 여러 개를 보관하는 배열 역할의 래퍼 애너테이션
```
- 모든 자바 애너테이션은 자동으로 java.lang.annotation.Annotation 인터페이스를 확장한 구조를 가진다.
- 그리고 런타임에서는 JVM이 생성한 프록시 객체로 존재하며, 리플렉션으로 접근할 때 Annotation 타입으로 동작한다.
```
public @interface MyAnnotation {
    String value();
}
// 내부적으로는 이것이 사실상 다음과 같은 인터페이스로 변환된다.
// 즉, @interface 키워드는 interface + extends Annotation의 문법적 설탕(syntactic sugar)이다.
public interface MyAnnotation extends java.lang.annotation.Annotation {
    String value();
}
// java.lang.annotation.Annotation 인터페이스의 구조
public interface Annotation {
    boolean equals(Object obj); // 애너테이션이 같은지 비교할 때 사용
    int hashCode(); // 해시코드 계산 (equals와 일관성 유지해야 함)
    String toString(); // 애너테이션을 문자열로 표현
    Class<? extends Annotation> annotationType(); // 이 애너테이션의 타입(Class)을 반환
}
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {
    String value();
}
@MyAnnotation("hello")
class Test {}
public static void main(String[] args) {
    MyAnnotation ann = Test.class.getAnnotation(MyAnnotation.class);
    System.out.println(ann.annotationType()); // interface MyAnnotation
    System.out.println(ann.annotationType() == MyAnnotation.class); // true
    System.out.println(ann instanceof Annotation); // true
}
// Test.class.getAnnotation(MyAnnotation.class): 이 메서드는 리플렉션이 만든 프록시 객체를 반환합니다.
// 이 객체는 MyAnnotation을 구현하면서 내부적으로 equals(), hashCode(), toString()을 자동 구현해 둡니다.
```
- 컴파일 시에 Javac(자바 컴파일러)가 자동으로 애너테이션의 바이트코드 클래스(.class)를 생성하고
- 내부적으로 Annotation 인터페이스를 구현하는 프록시(proxy) 객체를 만들어냅니다.
- 이건 JVM 내부에서 리플렉션 API를 통해 접근할 수 있어요.
- JVM이 런타임에 프록시 객체(proxy)를 만들어주는 것은 RetentionPolicy.RUNTIME일 때만입니다.
- CLASS나 SOURCE일 때는 JVM이 프록시 객체를 만들지도 않고, 인터페이스 구현도 하지 않습니다.
- 자바 컴파일 시점 — “인터페이스로 변환됨”
```
컴파일러(javac)는 다음과 같은 코드를 보면:
public @interface Example {
    String value();
}
다음과 같은 바이트코드 구조를 만듭니다.
public interface Example extends java.lang.annotation.Annotation {
    public abstract java.lang.String value();
}
즉, 애너테이션 선언부는 무조건 인터페이스 형태가 돼요.
이건 Retention 정책과 상관없이 모든 애너테이션이 동일합니다.
그래서 RetentionPolicy.SOURCE, CLASS, RUNTIME 상관없이 모두 인터페이스 형태예요.
런타임 로딩 시점 — JVM이 프록시를 만들까
이제 Class.getAnnotation() 같은 리플렉션 호출이 들어올 때 이야기입니다.
Example e = MyClass.class.getAnnotation(Example.class);
이때 상황은 다음 세 가지로 나뉘어요
Retention	JVM이 프록시 객체를 생성하나?	이유
SOURCE	No	애너테이션 정보가 .class 파일에 없음. JVM은 존재조차 모름
CLASS	No	.class 파일엔 있지만 JVM이 로딩 시 버림
RUNTIME	Yes	JVM이 클래스 메타데이터에 애너테이션 정보를 보존하고, 리플렉션 호출 시 프록시 객체를 생성
```
```
“프록시 객체(proxy)”는 정확히 뭐냐
JVM이 Example 애너테이션 정보를 읽어서 내부적으로 java.lang.reflect.Proxy를 이용해
“가짜 구현체”를 만듭니다. 대략 다음과 같은 느낌이에요:
class $Proxy0 implements Example {
    private final Map<String, Object> values = Map.of("value", "hello");
    @Override
    public String value() {
        return (String) values.get("value");
    }
    @Override
    public Class<? extends Annotation> annotationType() {
        return Example.class;
    }
    @Override
    public boolean equals(Object obj) { ... }
    @Override
    public int hashCode() { ... }
    @Override
    public String toString() { ... }
}
즉, Example.class.getAnnotation()이 반환하는 객체는 이런 “리플렉션 프록시”의 인스턴스입니다.
CLASS나 SOURCE 경우에는 상황이 다릅니다:
RetentionPolicy.SOURCE
애너테이션 정보가 컴파일 시점에만 존재하고 .class 파일에 기록조차 안 됩니다.
따라서 JVM은 이 애너테이션의 존재 자체를 모릅니다.
리플렉션 시에도 아무 객체가 만들어지지 않습니다.
@SourceAnn
class Test {}
Test.class.getAnnotation(SourceAnn.class); // 항상 null 반환
RetentionPolicy.CLASS
.class 파일에는 애너테이션 메타데이터가 들어있습니다. (바이트코드 수준에서는 보임)
하지만 클래스 로더가 JVM에 클래스를 적재할 때 이 정보는 버려집니다.
그래서 런타임 시점에는 존재하지 않으며, 프록시 생성도 없습니다.
즉, JVM은 아예 프록시를 만들 기회조차 없어요.
```
- 애너테이션은 기본적으로 상속되지 않는다.
- 자바의 애너테이션은 클래스 상속 구조와 무관하게 “붙은 클래스에만” 존재합니다.
- @Inherited의 등장 — “상속된 것처럼 보이게”
- @Inherited를 붙이면, 자바의 리플렉션 API(getAnnotation, isAnnotationPresent)가
- “부모 클래스의 애너테이션을 자식 클래스에서도 검색되게” 만들어 줍니다.
```
하지만 “진짜 상속”은 아니다.
리플렉션의 getAnnotation()은 내부적으로 이런 과정을 거칩니다.
Child 클래스에 MyAnnotation이 직접 붙어 있는지 확인
없으면 @Inherited 여부를 체크
부모 클래스(Parent)의 애너테이션을 찾아 반환
즉, 검색 순서를 조정하는 효과일 뿐 실제로 Child.class의 메타데이터에 @MyAnnotation이 추가되는 건 아닙니다.
for (Annotation ann : Child.class.getDeclaredAnnotations()) {
    System.out.println(ann);
}
출력:
(출력 없음)
Child 클래스의 “declaredAnnotations”(직접 선언된 애너테이션)은 비어 있습니다.
하지만:
for (Annotation ann : Child.class.getAnnotations()) {
    System.out.println(ann);
}
출력:
@MyAnnotation()
getAnnotations()은 부모의 애너테이션을 포함해서 반환합니다.
그래서 “상속된 것처럼 보이는” 거예요.
```
```
자바의 Class에는 애너테이션 관련해서 이런 메서드들이 있다.
Annotation[] getAnnotations(): 애너테이션 전부 반환 (상속 포함)
Annotation[] anns = Child.class.getAnnotations();
// 모든 애너테이션(상속 포함)을 배열로 반환
<A extends Annotation> A getAnnotation(Class<A> annotationClass): 지정한 애너테이션 한 개만 반환 (상속 포함)
MyAnnotation e = Child.class.getAnnotation(MyAnnotation.class);
// 이 클래스에 (또는 상속 계층상 부모에) @MyAnnotation이 있는가를 확인하는 메서드
// 타입 안전하게 MyAnnotation으로 받는다. 존재하지 않으면 null을 반환
Annotation[] getDeclaredAnnotations(): 직접 선언된 것만 반환 (상속 무시)
<A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass): 특정 애너테이션 한 개만, 직접 선언된 것만 반환
```
- @Inherited는 클래스 상속(class inheritance)에만 적용됩니다.
- 인터페이스 구현(interface implementation)에는 적용되지 않습니다.
- class Child extends Parent: 부모의 애너테이션이 상속처럼 보임
- class Impl implements MyInterface: 인터페이스의 애너테이션은 상속되지 않음
```
@Inherited의 정의
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inherited {}
자바 표준 라이브러리의 설명(Javadoc)을 보면 이렇게 되어 있어요:
"If an annotation type is marked as inherited,
then an annotation on a superclass of a class is inherited by its subclasses.
Annotations on interfaces are not inherited."
즉, superclass만 대상이고 interface는 아예 언급되지 않습니다.
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {}
@MyAnnotation
class Parent {}
class Child extends Parent {}
public static void main(String[] args) {
    System.out.println(Child.class.isAnnotationPresent(MyAnnotation.class)); // true
}
@MyAnnotation이 Child 클래스에는 직접 붙어있지 않지만,
부모(Parent)에 붙어 있으므로 상속된 것처럼 보이게 리플렉션 결과에 포함됩니다.
인터페이스 구현 — @Inherited 작동 안 함
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {}
@MyAnnotation
interface MyInterface {}
class Impl implements MyInterface {}
public static void main(String[] args) {
    System.out.println(Impl.class.isAnnotationPresent(MyAnnotation.class)); // false
}
@Inherited를 붙였는데도, 인터페이스에 선언된 애너테이션은 구현 클래스에 전달되지 않아요.
```
```
이건 언어 설계 철학적인 이유 때문이에요:
클래스 상속(inheritance)
"부모 클래스의 속성/행동을 자식이 물려받는다"는 게 자연스러운 상속 모델이에요.
애너테이션이 클래스의 의미적 메타데이터라면, 자식도 그 의미를 계승해야 하는 게 논리적이죠.
@Entity // JPA의 의미상 하위 클래스도 엔티티로 취급되어야 함
class BaseEntity {}
class User extends BaseEntity {} // 상속해도 @Entity의 의미 유지
인터페이스 구현(implementation)
구현체는 인터페이스의 계약(contract)을 따를 뿐, 인터페이스의 메타데이터를 상속해야 할 이유는 없음
인터페이스는 행동 규약만 정의할 뿐, 그 자체가 클래스의 의미를 결정하지 않아요.
@Marker
interface Service {}
class MyService implements Service {} // 단지 계약 이행, @Marker 의미는 직접 명시해야 함
즉, implements 관계는 “의미의 상속”이 아니라 “행동의 약속”이기 때문에
JVM은 인터페이스의 애너테이션을 자동으로 “상속처럼 보이게” 하지 않습니다.
리플렉션으로도 확인 가능
System.out.println(Arrays.toString(Impl.class.getAnnotations()));
클래스 상속 구조에서는 부모의 @Inherited 애너테이션이 포함되어 나옵니다.
인터페이스 구현 구조에서는 인터페이스의 애너테이션이 포함되지 않습니다.
단, 인터페이스의 애너테이션도 “수동으로”는 확인 가능함
리플렉션으로 인터페이스를 직접 탐색하면 애너테이션을 찾을 수 있습니다.
for (Class<?> i : Impl.class.getInterfaces()) {
    if (i.isAnnotationPresent(MyAnnotation.class)) {
        System.out.println("Interface has @MyAnnotation"); // Interface has @MyAnnotation
    }
}
+ 인터페이스끼리도 상속 안 됨
```
## 리플렉션, 애너테이션
- Field 객체는 클래스의 멤버 변수(필드)를 표현하는 객체
- field.get(obj)나 field.getLong(obj) 같은 메서드는 그 객체 안의 필드 값을 가져오는 방법
```
Field field = obj.getClass().getDeclaredField("name");
이렇게 하면 obj의 클래스에서 "name"이라는 필드에 대한 메타데이터를 담은 Field 객체가 생겨요.
이 Field는 다음 일을 할 수 있습니다:
field.getName() → 필드 이름 얻기
field.getType() → 필드 타입 얻기 (String, int, boolean 등)
field.get(obj)
→ 특정 객체(obj) 안에 들어 있는 그 필드의 실제 값 얻기
→ 필드 타입이 참조형(Object)일 때 사용 (ex: String, Integer, List, etc.)
field.getInt(Object obj) // 필드 타입이 int일 때 사용
field.getLong(Object obj) // 필드 타입이 long일 때 사용
field.getDouble(Object obj) // 필드 타입이 double일 때 사용
field.set(obj, value) → 특정 객체의 필드 값을 바꾸기
```
## 자바 기반 애노테이션

```java
import java.lang.annotation.Documented;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override {...}
// 자바 컴파일러가 메서드 재정의 여부를 체크해준다.
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value={CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE})
public @interface Deprecated {...}
// 더는 사용을 권장하지 않는 요소 (속성에 자세한 정보 쓴다.)
// 자바 컴파일러가 경고를 표시한다. (프로그램은 작동)
@TARGET({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, MODULE})
@Retention(RetentionPolicy.SOURCE)
public @interface SuppressWarnings {
    String[] value();
}
// 자바 컴파일러의 경고를 억제하는 애노테이션
// 값에는 all, deprecation, unchecked, serial, rawtypes, unused 등을 쓴다.
```
## 프레임워크
- 의존성 주입: 스프링은 리플렉션으로 객체의 필드나 생성자에 자동으로 의존성을 주입한다. 개발자는 @Autowired만 붙이면 된다.
- ORM(object-relational mapping): JPA는 애노테이션으로 자바 객체와 데이터베이스 테이블 간의 매핑을 정의한다. 예를 들어, @Entity, @Table, @Column 등의 애노테이션으로 객체-테이블 관계를 설정한다.
- AOP(aspect-oriented programming): 스프링은 리플렉션으로 런타임에 코드를 동적으로 주입하고, @Aspect, @Before, @After 등의 애노테이션으로 관점 지향 프로그래밍을 구현한다.
- 설정의 자동화: @Configuration, @Bean 등의 애노테이션으로 다양한 설정을 편리하게 적용한다.
- 트랜잭션 관리: @Transactional 애노테이션으로 메서드 레벨의 DB 트랜잭션 처리가 가능해진다.
- 리플렉션과 애노테이션으로 메타프로그래밍을 하면
- 비즈니스 로직에 집중하고, 보일러플레이트 코드를 줄일 수 있다.
# 15. HTTP 서버 활용
- 251020-java-adv2/src/http5 // 애노테이션 활용
- 251020-java-adv2/src/http6 // 기능 추가
```java
private void parseBody(BufferedReader reader) throws IOException {
    // 1단계: Content-Length 헤더 확인
    // Content-Length: 본문의 바이트 크기를 나타내는 헤더입니다.
    // 이게 없으면 본문이 없다는 의미이므로 메서드를 종료
    if (!headers.containsKey("Content-Length")) {
        return; // 본문이 없으면 종료
    }
    // 2단계: 본문 크기만큼 읽기
    // 본문의 크기만큼 문자 배열을 만들고, reader로부터 정확히 그만큼 읽어옵니다.
    int contentLength = Integer.parseInt(headers.get("Content-Length"));
    char[] bodyChars = new char[contentLength];
    int readLength = reader.read(bodyChars); // 바디 contentLength만큼 읽음
    // 3단계: 읽은 크기 검증
    // 예상한 크기와 실제 읽은 크기가 다르면 에러를 발생
    if (readLength != contentLength) {
        throw new IOException("contentLength만큼 읽지 못함");
    } else {
        // 4단계: 본문 파싱
        String body = new String(bodyChars);
        String contentType = headers.get("Content-Type");
        if ("application/x-www-form-urlencoded".equals(contentType)) {
            parseQueryParams(body); // URL 쿼리 파라미터와 같은 방식으로 파싱
        }
    }
}
```
- GET 요청: 파라미터가 URL에 (/add-member?id=1&name=test)
- `GET /add-member?id=user1&name=홍길동&age=25 HTTP/1.1`로 해도 등록 된다.
- POST 요청: 파라미터가 본문에 (id=1&name=test)
- `application/x-www-form-urlencoded`
- POST 요청 자체를 의미하는 것이 아니라, 데이터 인코딩 방식을 나타냄
- 본문(body)의 데이터가 URL 인코딩 형식으로 되어있다는 뜻
- id=user1&name=홍길동&age=25 이런 본문의 형식이 application/x-www-form-urlencoded
- multipart/form-data: 파일 업로드 시
- text/plain: 텍스트 본문
```java
public class HttpRequest {
    private final Map<String, String> queryParams = new HashMap<>(); // URL 쿼리
    private final Map<String, String> formParams = new HashMap<>(); // POST 본문
    // 통합해서 가져오기
    public String getParam(String key) {
        String value = queryParams.get(key);
        return value != null ? value : formParams.get(key);
    }
}
// 파라미터를 담는 맵을 다르게 하면 좀 더 명확하다.
```
```java
@Mapping("/add-member-form")
public void addMemberForm(HttpResponse response) {
    StringBuilder sb = new StringBuilder();
    sb.append("<h1>멤버 추가</h1>").append("\n");;
    sb.append("<form action='/add-member' method='POST'>"); // POST로 해야 한다.
    sb.append("<input type='text' name='id' placeholder='멤버 id'>");
    sb.append("<input type='text' name='name' placeholder='멤버 name'>");
    sb.append("<input type='text' name='age' placeholder='멤버 age'>");
    sb.append("<button type='submit'>추가</button>");
    sb.append("</form>");
    String body = sb.toString();
    response.writeBody(body);
}
```
- 1. RESTful 원칙
- GET: 데이터를 조회할 때 (읽기)
- POST: 데이터를 생성/추가할 때 (쓰기)
- 멤버를 추가하는 것은 서버의 데이터를 변경하는 작업이므로 POST가 맞습니다.
- 2. 보안 문제
- GET 제출하면: `http://example.com/add-member?id=user1&name=홍길동&age=25`
- URL에 데이터가 노출됨
- 브라우저 히스토리에 남음
- 서버 로그에 기록됨
- POST: 데이터가 본문(body)에 담겨서 전송됨 (URL에 노출 안 됨)
- 3. 멱등성(Idempotency)
- GET: 여러 번 호출해도 같은 결과 (조회)
- POST: 호출할 때마다 새로운 리소스 생성
- 여러 번 실행되는 건 같다. 실행 때마다 결과가 같으면 멱등성o, 다르면 멱등성x