# 10. 예외 처리1 - 이론
- 1. 프로그램 구성도
```
사용자 입력 --data--> Main
Main --sendMessage(data)--> NetworkService
NetworkService --connect(), send(data), disconnect()--> NetworkClient
NetworkClient --연결, data 전송, 연결 해제--> 외부 서버
```
- NetworkClient.java - 실제 네트워크 연결을 담당
- NetworkService.java - 비즈니스 로직과 에러 처리를 담당
- Main.java - 진입점
- 2. 실행 예시
```
전송할 문자: hello
http://example.com 서버 연결 성공
http://example.com 서버에 데이터 전송: hello
http://example.com 서버 연결 해제
```
- 3. 코드1
- 메인
```java
public class Main1 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        NetworkService1 service = new NetworkService1();
        while (true) {
            System.out.print("data 입력 (종료는 exit): ");
            String data = input.nextLine();
            if (data.equals("exit")) {
                break;
            } else {
                service.sendMessage(data);
            }
        }
    }
}
```
- 서비스
```java
public class NetworkService1 {
    public void sendMessage(String data) {
        String address = "http://example.com";
        NetworkClient1 client = new NetworkClient1(address);
        client.initError(data);
        String result1 = client.connect();
        if (isError(result1)) {
            System.out.println("error1: " + result1);
        } else {
            String result2 = client.send(data);
            if (isError(result2)) {
                System.out.println("error2: " + result2);
            }
        }
        client.disconnect();
    }
    private static boolean isError(String result) {
        return !result.equals("success");
    }
}
```
- 클라이언트
```java
public class NetworkClient1 {
    private final String address;
    public boolean connectError;
    public boolean sendError;
    public NetworkClient1(String address) {
        this.address = address;
    }
    public String connect() {
        if (connectError) {
            System.out.println(address + " 연결 오류");
            return "connectError";
        }
        System.out.println(address + " 연결");
        return "success";
    }
    public String send(String data) {
        if (sendError) {
            System.out.println(address + "에 " + data + " 전송 오류");
            return "sendError";
        }
        System.out.println(address + "에 " + data + " 전송");
        return "success";
    }
    public void disconnect() {
        System.out.println(address + " 연결 해제");
    }
    public void initError(String data) {
        if (data.contains("error1")) {
            connectError = true;
        }
        if (data.contains("error2")) {
            sendError = true;
        }
    }
}
```
- 코드1 문제점: 정상 흐름과 예외 흐름이 섞여 있다.
- 4. 자바 예외 처리 키워드
- try: 예외가 발생할 수 있는 코드 블록
- catch: 예외를 잡아서 처리
- finally: 예외 발생 여부와 관계없이 항상 실행
- throw: 예외를 직접 발생시킴
- throws: 메서드가 던질 수 있는 예외를 선언
- try-catch-finally 흐름
```java
try {
    //예외 발생 가능한 코드
} catch (구체적예외 e) {
    //해당 예외 처리
} catch (상위예외 e) {
    //나머지 예외 처리
} finally {
    //항상 실행 (생략 가능)
}
//try 블록 실행
//예외 발생 시 → 해당하는 catch 블록으로 이동
//예외 없으면 → catch 건너뜀
//finally는 무조건 실행 (예외 여부 상관없음)
```
- throw는 메서드 내부에서 예외를 직접 발생시킴
- `throw new IllegalArgumentException("오류 메시지");`
- throws는 메서드 선언부에 예외를 던진다고 명시
- `public void method() throws IOException { ... }` //메서드를 호출하는 쪽에서 처리하도록
- 5. 예외 클래스 계층 구조
```java
Object
  └─ Throwable (모든 예외의 최상위)
      ├─ Error (시스템 레벨 오류)
      │   └─ OutOfMemoryError 등
      │
      └─ Exception (애플리케이션 레벨 예외)
          ├─ SQLException (체크 예외)
          ├─ IOException (체크 예외)
          │
          └─ RuntimeException (언체크 예외)
              ├─ NullPointerException
              └─ IllegalArgumentException
```
- (1) Error
- 시스템 레벨의 심각한 오류
- 개발자가 처리할 수 없음 (복구 불가능)
- 예: OutOfMemoryError
- (2) Exception
- (2.1) 체크 예외 (Checked Exception)
- 컴파일러가 예외 처리를 강제함
- 반드시 try-catch 또는 throws 필요
- 안 하면 컴파일 에러
- 예: SQLException, IOException
- (2.2) 언체크 예외 (Unchecked Exception = 런타임 예외)
- RuntimeException과 그 하위 클래스
- 컴파일러가 체크하지 않음, 예외 처리 선택 사항
- try-catch 하지 않으면 자동으로 throws 실행
- throws 전파되다가 프로그램 종료
- 예: NullPointerException, IllegalArgumentException
- 6. catch 순서 규칙
- catch 블록은 위에서 아래로 순서대로 검사된다.
- 잘못된 순서
```java
try {
    //코드
} catch (Exception e) {           //상위 예외를 먼저 잡음
    //모든 예외를 여기서 다 잡아버림
} catch (IOException e) {          //도달 불가능 (컴파일 에러)
    //이 블록은 실행될 수 없음
}
```
- 올바른 순서
```java
try {
    //코드
} catch (IOException e) {          //구체적인 예외 먼저
    //IO 예외 처리
} catch (SQLException e) {         //다른 구체적 예외
    //DB 예외 처리
} catch (Exception e) {            //상위 예외는 마지막
    //나머지 예외 처리
}
```
- 상위 예외를 먼저 잡으면 모든 하위 예외가 걸려서 아래 catch에 도달할 수 없다.
- 도달 불가능한 코드(Unreachable Code)가 있으면 컴파일 에러 난다.
- 하위 예외 → 상위 예외 순서로 작성하거나, 상위 예외로만 처리한다.
- 7. 예외에서 부모가 더 넓다는 의미
- (1) 클래스 관점 (상속)
- 자식이 부모보다 많음 (확장)
- 자식 = 부모 기능 + 추가 기능
- (2) 예외 처리 관점 (범위)
- 부모가 자식을 포함함 (포괄)
- 부모 = 자식들 전체를 아우르는 개념
- (3) 자바의 다형성(Polymorphism) 때문
- throw new `IOException`() 발생
- catch (Exception e)에 도달
- IOException instanceof Exception == true
- 블록 실행하고 finally로 간다.
- 8. 폭탄 돌리기처럼 예외 처리
- 예외 발생 시 잡아서 처리, 처리 안 되면 밖으로 던져
- Main -> Service 호출
- Service -> Client 호출
- Client 예외 발생
- Client -> Servie 예외 던짐
- (8.1) Service 예외 처리: 후에는 애플리케이션 로직 정상 흐름
- Service -> Main 정상 흐름 반환
- (8.2) Service가 예외 처리 못하면 Main으로 던짐
- (8.3) Main이 예외 처리 못하면 main() 밖으로 던짐
- 예외 종류와 발생 경로(Stack Trace)를 출력 + 프로그램 종료
```java
Exception in thread "main" except2.NException: http://example.com에 error2 전송 오류
at except2.NetworkClient2.send(NetworkClient2.java:17)
at except2.NetworkService2.sendMessage(NetworkService2.java:9)
at except2.Main2.main(Main2.java:14)
Process finished with exit code 1
```
- 9. 예외를 잡거나 던질 때 지정한 예외의 자식들도 함께 처리 가능
- throw - 예외를 직접 발생시킴
```java
public void checkAge(int age) {
    if (age < 0) {
        throw new IllegalArgumentException("나이는 음수일 수 없습니다");
    }
}
```
- throws - 예외를 호출한 곳으로 던짐 (위임)
```java
public void readFile() throws IOException {
    //파일 읽기 - IOException 발생 가능
    //여기서 처리 안 하고 호출한 쪽에 던짐
}
```
- 던질 때 자식도 함께 처리
```java
//메서드 선언
public void connectDB() throws Exception {
    //실제로는 SQLException이 발생할 수 있음
}
//throws Exception으로 선언하면:
//SQLException도 던질 수 있음 (자식이니까)
//IOException도 던질 수 있음 (자식이니까)
//RuntimeException도 던질 수 있음 (자식이니까)
```
- 구체적인 코드
```java
//SQLException을 던지지만, Exception으로 선언 가능
public void method1() throws Exception {  //부모로 선언
    throw new SQLException("DB 오류");     //자식을 던짐
}
```
- 10. 선언 타입과 실제 타입은 다르다.
- 선언(throws): Exception, 실제로 던지는 것: IOException 객체
```java
public void method() throws Exception {
    throw new IOException("오류");
}
```
- instanceof는 "실제 객체"를 본다.
```java
try {
    method();
    
} catch (Exception e) {
    e instanceof IOException   //true
    e instanceof Exception     //true
    e instanceof Object        //true
}
```
- 실제로 던져진 객체 IOException이다.
```java
IOException realObject = new IOException();  //실제 생성된 객체
Exception e = realObject;  //Exception 타입 변수에 담김 (다형성)
//instanceof는 변수 타입이 아니라 실제 객체를 검사
e instanceof IOException  //true
```
- 11. 예외 던지기와 반환은 다르다.
- (1) return (반환)
- 정상적인 흐름
- 메서드가 성공적으로 끝남
- 전달: 값을 돌려줌
- 받는 방법: 변수에 할당 (처리 필수 아님)
```java
public String getName() {
    return "홍길동";  //값 반환
}
String name = getName();  //정상 진행
System.out.println(name);  //계속 실행
```
- (2) throw/throws (던지기)
- 비정상적인 흐름
- 메서드가 문제를 만나서 흐름 중단됨
- 전달: 예외 객체를 던짐
- "밖으로 던진다" = 예외가 메서드를 뚫고 올라감 (전파)
- 받는 방법: try-catch로 잡음 (체크 예외는 처리 필수)
```java
public String getName() {
    throw new RuntimeException("오류");  //예외 발생
}
String name = getName();  //여기서 터짐
System.out.println(name);  //실행 안 됨
```
- (3) 정상 흐름으로 돌아간다 의미
- 예외를 잡으면 프로그램이 계속 실행됨
- 예외를 안 잡으면 프로그램 종료
- 12. throw, throws
- (1) throw - 예외를 직접 발생시키기
- 메서드 안에 사용
- new 키워드로 예외 객체 생성
- 실제로 예외를 발생시킴
- (2) throws - 예외를 던질 수 있다고 알림
- 메서드 선언부에 사용
- 예외를 처리하지 않고 호출한 쪽에 떠넘김
- 경고 표시 같은 것
- (3) throw만 사용 (RuntimeException)
```java
public void divide(int a, int b) {
    //throws 없음 (RuntimeException은 생략 가능)
    if (b == 0) {
        throw new IllegalArgumentException("0으로 나눌 수 없음");
        //throw로 예외 발생
    }
}
```
- (4) throw + throws 함께 사용 (체크 예외)
```java
public void readFile(String path) throws IOException {
    //throws: IOException을 던질 수 있다고 선언
    if (path == null) {
        throw new IOException("경로 없음");
        //throw: 실제로 예외 발생
    }
}
```
- (5) throws만 사용 (예외 전파)
```java
//예외 생성 및 전파
public void dangerousMethod() throws IOException {
    throw new IOException("파일 오류");
}
//try-catch로 잡기 (처리)
try {
    dangerousMethod();
} catch (IOException e) {
    System.out.println("예외 처리");
}
//또 던지기 (전파)
public void caller() throws IOException {
    dangerousMethod();
}
```
- 13. 예외 코드
- Exception을 상속받으면 체크 예외가 된다.
- RuntimeException을 상속받으면 언체크 예외가 된다.
- CustomException
```java
public class CustomException extends Exception {
    public CustomException(String message) {
        super(message);
    }
}
//Exception 클래스를 상속받는 새 클래스 CustomException을 정의
//CustomException은 체크 예외(Checked Exception) 로 동작
//컴파일러가 이 예외를 처리하거나 throws로 던져야 한다고 강제
//public CustomException(String message)
//생성자(Constructor)
//예외 객체를 만들 때, 예외 메시지를 문자열로 받도록 한다.
//super(message)
//예외 메시지를 부모 클래스에 전달해서
//getMessage() 메서드로 그 메시지를 꺼낼 수 있게 한다.
//사용 예시
/*
public class Example {
    public static void main(String[] args) {
        try {
            checkAge(15);
        } catch (CustomException e) {
            System.out.println("예외 발생: " + e.getMessage());
        }
    }
    static void checkAge(int age) throws CustomException {
        if (age < 20) {
            throw new CustomException("20세 이상 아님");
        }
    }
}
 */
//Throwable, Exception 클래스 단순화 버전
/*
public class Throwable implements Serializable {
    private String detailMessage; //예외 메시지가 실제로 저장되는 곳
    public Throwable(String message) {
        detailMessage = message;
    }
    public String getMessage() {
        return detailMessage;
    }
}
public class Exception extends Throwable {
    public Exception(String message) {
        super(message); //Throwable(String message) 호출
    }
}
 */
//detailMessage는 private으로 되어 있다.
//Exception, CustomException에서 직접 접근 안 된다.
//같은 필드를 자식 클래스에 선언하면, 부모 타입으로 받았을 때 다르게 호출된다.
```
- CustomException2
```java
public class CustomException2 extends RuntimeException {
    public CustomException2(String message) {
        super(message);
    }
}
```
- 14. 코드2
- 단축키: ctrl + alt + t (try/catch로 감싸기)
- client.disconnect();를 finally에 쓰지 않으면
- 잡지 않은 에러 발생 시 외부 시스템에 리소스가 반환이 안 된다.
- 즉, finally가 실행되고 나서 잡지 못한 에러가 던져진다.
- NException, NException2 전부 extends Exception로 했다.
- throws를 NetworkClient2가 하기 때문이다.
- 250929-java-mid1/src/except2/Main2.java
- 250929-java-mid1/src/except2/NetworkClient2.java
- 250929-java-mid1/src/except2/NetworkService2.java
- 250929-java-mid1/src/except2/NException.java
- 250929-java-mid1/src/except2/NException2.java
- 15. 코드3
- (1) 체크 예외 (Exception)
- throws Exception은 최소한으로 사용하는 게 좋다.
- 구체적인 예외 타입을 선언하는 게 명확하다.
- 복구 가능한 예외는 잡아서 처리해야 한다.
- (2) 언체크 예외 (RuntimeException)
- 외부 시스템 오류, 프로그래밍 오류는 대부분 언체크 예외다.
- 대부분 잡아도 복구 불가능하다.
- 복구 가능한 것만 선별해서 처리하고
- 나머지는 자연스럽게 던져지도록 둔다. (throws 선언 불필요)
- (3) 공통 예외 처리
- 언체크 예외는 자동으로 전파된다.
- Controller나 Facade 같은 경계 지점에서 공통으로 잡아서
- 사용자와 개발자에게 적절한 정보 제공한다.
- 250929-java-mid1/src/except3/Main3.java //exceptionHandler
- 250929-java-mid1/src/except3/NetworkClient3.java
- 250929-java-mid1/src/except3/NetworkService3.java //try-finally
- 250929-java-mid1/src/except3/RException.java
- 250929-java-mid1/src/except3/RException2.java
- 16. printStackTrace()
- 스택 트레이스 = 예외가 발생하기까지의 메서드 호출 경로를 보여줌
- 아래에서 위로 읽으면 실행 순서
```java
java.lang.RuntimeException: 네트워크 오류
    at NetworkClient.connect(NetworkClient.java:15)
    at NetworkService3.sendMessage(NetworkService3.java:23)
    at Main.main(Main.java:12)
예외 타입: 메시지
at 메서드명(파일명:라인번호)  ← 예외 발생 지점
at 메서드명(파일명:라인번호)  ← 그걸 호출한 곳
at 메서드명(파일명:라인번호)  ← 또 그걸 호출한 곳
```
- 출력의 종류
```java
//1. 기본 (System.err에 출력 - 빨간색)
e.printStackTrace();
//2. System.out에 출력 (검은색)
e.printStackTrace(System.out);
//3. 파일에 출력
PrintWriter writer = new PrintWriter("error.log");
e.printStackTrace(writer);
```
- 개발 중엔 e.printStackTrace();로 콘솔에 출력해서 디버깅한다.
- 운영 환경에서는
```java
//로그 파일에 기록
logger.error("오류 발생", e);  //스택 트레이스 포함
//사용자에게는 간단한 메시지만
System.out.println("일시적인 오류가 발생했습니다.");
```
- 16. try-with-resources
```java
try (리소스 선언) {
    //리소스를 사용하는 코드
} catch (Exception e) {
    //예외 처리
}
```
- (1) 자동 리소스 해제
- try 블록이 끝나면 자동으로 close() 메서드가 호출된다.
- 예외가 발생하든 안하든 무조건 호출된다.
- finally 블록에서 수동으로 닫을 필요가 없다.
- (2) 사용 가능한 리소스
- AutoCloseable 인터페이스를 구현한 객체만 사용 가능
- 대부분의 I/O 관련 클래스들이 해당
- (3) try-with-resources 없을 때
```java
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("file.txt"));
    String line = br.readLine();
    System.out.println(line);
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (br != null) {
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
- try-with-resources 사용
```java
try (BufferedReader br = new BufferedReader(new FileReader("file.txt"))) {
    String line = br.readLine();
    System.out.println(line);
} catch (IOException e) {
    e.printStackTrace();
}
```
- 리소스 여러 개일 때는 세미콜론으로 구분
```java
try (FileInputStream fis = new FileInputStream("input.txt");
     FileOutputStream fos = new FileOutputStream("output.txt")) {
    //파일 복사 작업
    int data;
    while ((data = fis.read()) != -1) {
        fos.write(data);
    }
} catch (IOException e) {
    e.printStackTrace();
}
```
- AutoCloseable 인터페이스
```java
public interface AutoCloseable {
    void close() throws Exception;
}
//try-with-resources가 블록이 끝날 때 close() 메서드를 호출
```
- FileInputStream은 AutoCloseable을 구현했으므로 사용 가능
- String은 AutoCloseable을 구현하지 않았으므로 에러
- 구현하는 클래스를 직접 만들어 쓸 수 있다.
```java
public class MyResource implements AutoCloseable {
    public MyResource() {
        System.out.println("리소스 열림");
    }
    public void doSomething() {
        System.out.println("작업 수행");
    }
    @Override
    public void close() {
        System.out.println("리소스 닫힘");
    }
}
try (MyResource resource = new MyResource()) {
    resource.doSomething();
} //자동으로 close() 호출
```
---