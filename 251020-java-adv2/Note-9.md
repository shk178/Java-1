- 251020-java-adv2/src/chat/client/WriteHandler.java
- System.in은 JVM 전체에서 공유되는 입력 스트림입니다.
- System.in.close()를 호출하면 다른 스레드에서 System.in을 사용하는 모든 Scanner나 입력 관련 코드가 예외를 던지게 됩니다.
- 따라서 WriteHandler.close()에서 System.in.close()를 호출하면,
- main 스레드나 다른 스레드에서 입력을 시도할 경우 NoSuchElementException이나 IllegalStateException이 발생할 수 있습니다.
- 이 예외는 WriteHandler.run() 내부에서는 잡히지만, main 메서드에서 직접 잡을 수는 없습니다.
- 왜냐하면 WriteHandler는 Runnable로 별도 스레드에서 실행되고, 예외가 그 스레드 내에서 발생하고 처리되기 때문이에요.
- System.in.close()는 일반적으로 프로그램 전체 종료 시점에만 호출하는 것이 안전합니다.
- 만약 특정 스레드에서만 입력을 중단하고 싶다면: scanner.close()만 호출
- 이렇게 하면 해당 Scanner만 닫고, System.in은 열려 있으므로 다른 스레드에 영향 없음
- 또는 System.in을 닫지 않고 플래그로 종료 제어 (입력 루프를 종료하는 방식)
- closed 플래그를 활용해서 루프를 빠져나오게 하고, System.in은 건드리지 않는 방식
- 또는 PipedInputStream 등으로 입력 스트림 분리
- 고급 방법이지만, System.in을 직접 사용하지 않고 별도의 입력 스트림을 만들어 스레드 간 간섭을 피할 수 있어요.
- System.in.close()는 IOException을 던질 수 있는 메서드이긴 하지만, 실제로는 거의 예외가 발생하지 않습니다.
- System.in은 표준 입력 스트림이고, JVM이 시작될 때 자동으로 열립니다.
- 이 스트림을 닫는다고 해서 내부적으로 복잡한 I/O 작업이 수행되는 건 아니기 때문에
- 대부분의 환경에서는 IOException 없이 정상적으로 닫힙니다.
- 하지만 자바의 close() 메서드는 Closeable 인터페이스를 따르기 때문에 언제든지 I/O 오류가 발생할 수 있다는 가능성을 열어두고 IOException을 선언합니다.
- 실제로 IOException 발생할 수 있는 경우
- 입력 스트림이 이미 닫혀 있거나, OS 수준에서 뭔가 문제가 생긴 경우
- 커스텀 입력 스트림을 System.setIn()으로 바꿨을 때, 그 스트림이 close()에서 예외를 던지는 경우
- 하지만 일반적인 콘솔 기반 프로그램에서는 System.in.close()가 IOException을 던지는 경우는 매우 드뭅니다.
- 언제 NoSuchElementException이 발생하나
- 1. Scanner.nextLine() 또는 next() 호출 시 입력이 더 이상 없을 때
- 예: System.in이 닫혔거나, 입력 스트림이 EOF(End of File)에 도달한 경우
- 2. 다른 스레드에서 System.in.close()를 호출한 경우
- WriteHandler에서 System.in.close()를 호출하면, 다른 클래스나 스레드에서 Scanner를 통해 입력을 받으려 할 때 NoSuchElementException이 발생할 수 있습니다.
- 3. 입력 스트림이 외부에서 강제로 닫힌 경우
- 예를 들어 서버나 클라이언트가 종료되면서 스트림이 닫히면, 이후 입력을 시도하는 코드에서 예외가 발생할 수 있습니다.
- 안전하게 처리하는 방법
- scanner.hasNextLine() 또는 scanner.hasNext()로 먼저 확인하고 nextLine()을 호출
- System.in을 닫지 않고 Scanner.close()만 호출하거나, 입력을 중단할 때는 플래그를 사용해 루프를 종료
- 예외를 try-catch로 감싸서 안전하게 종료 처리
- 251020-java-adv2/src/chat2 // 커맨드 패턴 적용
# 10. HTTP 기본 이론
- 1. HTTP 요청/응답 메시지에 전송
- HTML, TEXT
- IMAGE, 음성, 영상, 파일
- JSON, XML (API)
- 거의 모든 형태의 데이터 전송 가능
- 2. HTTP 메시지 구조
- start-line (시작 라인)
- header (헤더)
- empty line (공백 라인, CRLF)
- message body (본문)
- 3. 시작 라인 - 요청 메시지
- HTTP 메서드 (GET, POST, PUT, DELETE 등)
- 요청 대상 (`absolute-path[?query]`)
- HTTP 버전
```
GET /search?q=hello&hl=ko HTTP/1.1 Host: www.google.com
HTTP 메서드: GET
절대 경로: /search (/로 시작하는 경로)
쿼리: q=hello&hl=ko (key1=val1&key2=val2 형식)
HTTP 버전: HTTP/1.1
```
- 4. 시작 라인 - 응답 메시지
- HTTP 버전
- HTTP 상태 코드 (200=성공, 400=요청 오류, 500=서버 오류)
```
HTTP/1.1 200 OK
// 상태 코드에 OK처럼 설명이 덧붙여진다.
Content-Type: text/html;charset=UTF-8
Content-Length: 3423

<html>
  <body>...</body>
</html>
```
- 5. 헤더
- HTTP 전송에 필요한 모든 부가정보
- name: value (필드 이름은 대소문자 구분 없음)
- 메시지 바디의 내용, 메시지 바디의 크기, 압축, 인증, 요청 클라이언트(브라우저) 정보, 서버 애플리케이션 정보, 캐시 관리 정보
- 표준 헤더가 많다. (https://en.wikipedia.org/wiki/List_of_HTTP_header_fields)
- 필요 시 임의의 헤더 추가 가능하다. (customname: customvalue)
```
GET /search?q=hello&hl=ko HTTP/1.1 Host: www.google.com
- 헤더: Host: www.google.com

HTTP/1.1 200 OK
// 상태 코드에 OK처럼 설명이 덧붙여진다.
Content-Type: text/html;charset=UTF-8
Content-Length: 3423

<html>
  <body>...</body>
</html>
- 헤더: Content-Type, Content-Length
```
- 6. 메시지 바디
- 헤더와 바디 사이에 공백 라인
- 실제 전송할 데이터
- HTML 문서, 이미지, 영상, JSON 등 byte로 표현할 수 있는 모든 데이터 전송
```
<html>
  <body>...</body>
</html>
```
### GET
- 리소스 조회
- 서버에 전달하고 싶은 데이터는 query를 통해서 전달
- 메시지 바디 사용하지 않음 (서버가 지원하면 가능)
- GET의 응답 메시지가 바디에 리소스 가져온다.
### POST
- 요청 데이터 처리
- 메시지 바디를 통해 서버로 요청 데이터를 전달
- 서버는 요청 데이터를 처리
- 예: 신규 리소스 등록, 프로세스 처리 등
```
(요청 메시지)
POST /add-member HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Content-Length: 22

id=id1&name=name1&age=20

(응답 메시지)
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8
Content-Length: 38

<html>
  <body>save ok</body>
</html>
```
- 251020-java-adv2/src/http/Server1Main.java
- `\r\n`: 캐리지 리턴(커서를 현재 줄의 맨 앞으로 이동) + 라인 피드(커서를 다음 줄로 이동)
- 초기 컴퓨터 시스템마다 줄바꿈 방식이 달랐기 때문
- 서로 다른 브라우저에서 같은 주소로 요청했을 때 시간 차가 생기는 이유
- 서버가 요청을 순차적으로 처리하기 때문
### 단일 스레드 처리
```java
while (true) {
    Socket socket = serverSocket.accept();
    process(socket);
} // 이 모든 작업이 하나의 스레드에서 순차적으로 실행
```
- 1. 브라우저 A가 먼저 요청을 보냄 → 서버가 `accept()`로 연결 받고 `process()` 실행
- 2. `process()` 안에서 `Thread.sleep(5000)`으로 5초 대기
- 3. 그 동안 브라우저 B가 요청을 보내도 → 서버는 아직 A의 요청을 처리 중이므로 B의 요청은 `accept()`에서 대기
- 4. A의 요청 처리가 끝나고 나서야 B의 요청을 `accept()`하고 처리 시작
- 즉, 요청이 동시에 들어와도 서버는 하나씩만 처리하므로 후속 요청은 앞선 요청이 끝날 때까지 기다려야 한다.
- 각 요청을 별도의 스레드에서 처리해야 한다.
```java
while (true) {
    Socket socket = serverSocket.accept();
    new Thread(() -> process(socket)).start();
} // 여러 브라우저가 동시에 요청해도 각 요청이 병렬로 처리되어 응답할 수 있다.
```
- 251020-java-adv2/src/http/Server2Main.java // 요청을 별도의 스레드에서 처리