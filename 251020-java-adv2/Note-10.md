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
# 15. HTTP 서버 활용