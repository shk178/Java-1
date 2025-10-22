## XML, JSON, 데이터베이스
- 회원 객체와 같이 구조화된 데이터를 통신할 때 사용하는 데이터 형식
- 1. 객체 직렬화의 한계
- 클래스 구조가 변경되면 이전에 직렬화된 객체와의 호환성 문제 발생
- 자바 직렬화는 자바 플랫폼에 종속적이어서 다른 언어나 시스템과의 상호 운용성이 떨어진다.
- 직렬화/역직렬화 과정이 상대적으로 느리고 리소스를 많이 사용한다.
- 직렬화된 형식을 커스터마이즈하기 어렵다.
- 직렬화된 데이터의 크기가 상대적으로 크다.
- 2. XML
```xml
<member>
    <id>id1</id>
    <name>name1</name>
    <age>20</age>
</member>
```
- 플랫폼 종속성 문제가 해결되었다.
- 유연하지만 복잡하고 무거웠다.
- 태그를 포함한 XML 문서는 크기가 커서 네트워크 전송 비용이 컸다.
- 3. JSON
```json
{ "member": { "id": "id1", "name": "name1", "age": 20 } }
```
- 가볍고 자바스크립트와 호환된다.
- 텍스트 기반 포맷이어서 디버깅과 개발이 쉽다.
- 웹 환경에서 표준 데이터 교환 포맷이 되었다.
- 4. Protobuf, Avro
- JSON보다 작은 용량으로 더 빠르게 통신할 수 있다.
- JSON보다 호환성은 덜하다. 사람이 읽기 어렵다.
- byte 기반에 용량과 성능 최적화가 되어 있다.
- 5. 데이터베이스
- 어떤 형식이든 데이터를 저장할 때 파일에 저장하는 방식은 한계가 있다.
- 데이터의 무결성 보장이 어렵다. 동시 접근 등 일관성 유지 어렵다.
- 데이터 검색과 관리가 비효율적이다.
- 보안 문제가 있다. (접근 제어, 암호화 등)
- 대규모 데이터의 효율적인 백업 및 복구가 필요하다.
- 이런 문제들을 해결하는 서버 프로그램이 데이터베이스다.
# 6. File, Files
- 파일 또는 디렉토리를 다룰 때 File, Files, Path 클래스를 사용한다.
- 251020-java-adv2/src/files/FileMain.java // File
- 251020-java-adv2/src/files/File1Main.java // Path + Files
- 251020-java-adv2/src/files/File2Main.java // File
- 251020-java-adv2/src/files/File3Main.java // Path + Files
- Files로 문자로 된 파일 읽기: File4Main.java
- Files로 문자로 된 파일 라인 단위로 읽기: File5Main.java
- 파일 복사 최적화: File6Main.java, File7Main.java, File8Main.java
- File5Main.java: Files.readAllLines(path) 대신 Files.lines(path)를 쓸 수도 있다.
## 자바 파일과 스트림
### 자바 I/O
- 추상화 계층: 자바는 운영체제와 하드웨어의 차이를 추상화하여 플랫폼 독립적인 I/O를 제공. JVM이 네이티브 시스템 콜을 래핑하는 방식으로 구현
- 스트림 기반 접근: 데이터를 연속된 바이트 또는 문자의 흐름으로 취급. 파일/네트워크/메모리 등 다양한 소스를 통일된 방식으로 처리 가능
- 데코레이터 패턴: 기본 스트림을 여러 레이어로 감싸 기능을 확장. BufferedInputStream, DataInputStream 등
### 자바 파일 시스템
- Java 1.0 (1996): java.io.File 클래스 도입. 파일 메타데이터 접근과 기본 조작 기능 제공 한다.
- Java 1.4 (2002): NIO (New I/O) 패키지 추가. 채널과 버퍼 기반 I/O로 성능 개선. 논블로킹 I/O 지원 시작
- Java 7 (2011): NIO.2 (java.nio.file) 도입. Path, Files 클래스로 현대적 파일 시스템 API 제공. 심볼릭 링크, 파일 속성, 워처 서비스 지원
- Java 8 (2014): 스트림 API 통합. Files.lines()로 함수형 스타일 파일 처리 가능. try-with-resources로 자원 관리 개선
### 바이트 스트림, 문자 스트림
- 바이트 스트림 (InputStream/OutputStream)
- 8비트 바이트 단위로 데이터를 처리. 이미지, 오디오, 비디오 등 바이너리 데이터에 적합
- 네이티브 시스템 콜 read()/write()를 직접 호출하여 OS 커널과 통신
- FileInputStream, FileOutputStream
- BufferedInputStream, BufferedOutputStream
- DataInputStream, DataOutputStream
- 문자 스트림 (Reader/Writer)
- 16비트 유니코드 문자 단위로 데이터를 처리. 텍스트 파일과 문자열 데이터에 적합
- 내부적으로 CharsetEncoder/Decoder를 사용해 바이트와 문자 간 변환을 수행
- FileReader, FileWriter
- BufferedReader, BufferedWriter
- InputStreamReader, OutputStreamWriter
### FileInputStream 내부 구현 분석
- FileInputStream은 파일로부터 바이트를 읽는 가장 기본 클래스
- FileInputStream.java, FileInputStream.c
- 1. 파일 디스크립터 획득
- 생성자에서 네이티브 메서드 open0()을 호출해 OS로부터 파일 디스크립터(fd)를 받는다.
- fd는 커널의 파일 테이블 엔트리를 가리킨다.
- 2. read() 메서드 호출
- 자바 레벨에서 read()를 호출하면 네이티브 메서드 read0()로 전달된다.
- JNI를 통해 C 코드로 구현돼 있다.
- 3. 시스템 콜 실행
- C 레벨에서 POSIX read() 시스템 콜을 호출한다.
- 커널이 페이지 캐시를 확인하고 없으면 디스크 I/O를 수행한다.
- 4. 데이터 복사
- 커널 버퍼의 데이터가 JVM 힙의 바이트 배열로 복사된다.
- 이 과정에서 컨텍스트 스위칭과 메모리 복사 오버헤드가 발생한다.
### 버퍼링의 중요성
- 시스템 콜을 비용이 크다: 유저 모드에서 커널 모드로 전환하고, 컨텍스트를 저장하고, I/O를 수행한 후 다시 복원하는 과정
- BufferedInputStream의 기본 버퍼 = 8KB 바이트 배열 버퍼
### NIO 채널과 버퍼 아키텍처
- Java NIO는 기존 스트림 기반 I/O의 한계를 극복하기 위해 설계됨
- 1. 채널
- 양방향 데이터 전송 통로다.
- 스트림과 달리 읽기/쓰기 동시 수행 가능
- FileChannel, SockerChannel 등이 있다.
- 논블로킹 I/O 지원
- 직접 메모리 접근 가능
- 파일 잠금, 메모리 맵 파일 지원
- 2. 버퍼
- 데이터를 담는 컨테이너로, 채널과 상호작용한다.
- position, limit, capacity 세 가지 포인터로 상태를 관리한다.
- ByteBuffer, CharBuffer, IntBuffer 등
- Direct Buffer: OS 메모리 직접 사용
- Heap Buffer: JVM 힙 메모리 사용
- 3. 셀렉터
- 단일 스레드로 여러 채널을 모니터링한다.
- 이벤트 기반 I/O로 높은 동시성 달성 가능
- epoll/kqueue 등 OS 메커니즘 활용
- C10K 문제 해결
- Netty, Undertow 등의 기반 기술
### FileChannel의 메모리 맵 파일
- Memory-Mapped Files의 원리
- FileChannel.map() 메서드는 파일의 내용을 가상 메모리에 직접 매핑한다.
- OS의 mmap() 시스템 콜을 호출하여 구현된다.
- 파일이 프로세스의 주소 공간에 매핑되면, 파일 접근이 메모리 접근으로 변환된다.
- 페이지 폴트가 발생하면 커널이 자동으로 디스크에서 페이지를 로드한다.
```java
FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
// 버퍼를 통한 접근이 메모리 접근과 동일해짐
byte b = buffer.get(1000);
```
- 성능 이점:
- read()/write() 시스템 콜 불필요
- 커널과 JVM 간 데이터 복사 제거
- OS 페이지 캐시 직접 활용
- 대용량 파일을 랜덤 액세스로 읽을 때 효과적
- 데이터베이스와 검색 엔진에서 주로 사용
- 주의사항:
- 매핑 해제를 명시적으로 할 수 없음
- 파일 크기가 크면 주소 공간 고갈 가능
- 32비트 JVM에서는 2GB 제한
### Direct / Heap ByteBuffer
- 1. Heap ByteBuffer
- JVM 힙 메모리에 할당
- ByteBuffer.allocate()로 생성
- 가비지 컬렉션이 관리
- 생성/해제 빠르다.
- I/O 시 임시 버퍼로 복사 필요
- 2. Direct ByteBuffer
- JVM 힙 외부의 네이티브 메모리에 할당
- ByteBuffer.allocateDirect()로 생성
- OS가 직접 접근할 수 있다. (커널 직접 접근)
- I/O 시 복사 불필요
- 생성/해제 느림
- 메모리 누수 위험
- 채널에서 Heap Buffer를 사용하면 JVM이 내부적으로 임시 Direct Buffer 생성해 데이터 복사한 후 I/O를 수행한다.
- 채널을 통한 I/O 작업 시 Direct Buffer를 사용하는 게 낫다.
### 문자 인코딩 처리 메커니즘
- Charset API로 바이트와 문자 간 변환 한다.
- 1. FileInputStream이나 FileChannel이 디스크에서 바이트 시퀀스를 읽어온다.
- 2. 지정된 문자셋의 CharsetDecoder가 바이트를 문자로 변환한다.
- 멀티바이트 문자셋에서 버퍼 경계에서 문자가 잘리면 디코더가 다음 읽기에서 완성한다.
- 3. 변환된 문자들이 CharBuffer에 저장되어 애플리케이션에 전달된다.
```java
// InputStreamReader 내부 구현
Charset charset = Charset.forName("UTF-8");
CharsetDecoder decoder = charset.newDecoder();
// 바이트 버퍼를 문자 버퍼로 변환
ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
CharBuffer charBuffer = CharBuffer.allocate(1024);
CoderResult result = decoder.decode(byteBuffer, charBuffer, false);
```
### Files 클래스의 내부 동작
- Files 클래스는 파일 작업을 위한 고급 유틸리티 메서드를 제공
- FileSystemProvider로 구현됨
- 1. Path 객체 생성
- Path는 파일 경로를 나타내는 인터페이스
- Paths.get()이나 Path.of()를 통해 생성됨
- 내부적으로 현재 플랫폼의 FileSystem 사용
- 2. FileSystemProvider 조회
- 각 FileSystem은 연결된 FileSystemProvider를 갖는다.
- 기본적으로 sun.nio.fs.WindowsFileSystemProvider 또는 UnixFileSystemProvider가 사용됨
- 3. 네이티브 구현 호출
- Provider는 플랫폼별 네이티브 메서드를 호출
- 예를 들어 Files.readAllBytes()는 내부적으로 FileChannel을 열고 read() 시스템 콜을 실행
```java
public static byte[] readAllBytes(Path path) throws IOException {
    try (SeekableByteChannel sbc = Files.newByteChannel(path); InputStream in = Channels.newInputStream(sbc)) {
        long size = sbc.size();
        if (size > MAX_BUFFER_SIZE) 
            throw new OutOfMemoryError();
        return read(in, (int)size);
    }
}
```
- 4. 예외 처리 및 변환
- 네이티브 에러 코드를 IOException 계층의 적절한 예외로 변환
- NoSuchFileException, AccessDeniedException 등
### 논블로킹 I/O와 Selector의 작동 원리
- 1. 블로킹 I/O
- 하나의 스레드가 I/O 작업을 수행하면 완료될 때까지 블로킹된다.
- 1만 개의 동시 연결을 처리하려면 1만 개의 스레드가 필요하며, 이는 메모리와 컨텍스트 스위칭 비용이 막대하다.
- 2. 논블로킹 I/O
- 채널을 논블로킹 모드로 설정하면 read()/write()가 즉시 반환된다.
- Selector를 사용해 여러 채널을 모니터링하고, I/O 준비가 된 채널만 처리한다.
- 3. Selector의 내부 메커니즘
- Selector는 OS의 다중 I/O 이벤트 알림 메커니즘을 활용한다.
- Linux: epoll - O(1) 시간복잡도
- BSD/macOS: kqueue - 고성능 이벤트 큐
- Windows: IOCP - 완료 포트 모델
- selector.select()를 호출하면:
- (1) 등록된 모든 채널의 fd를 epoll_wait()에 전달한다.
- (2) 커널이 이벤트 발생 시까지 대기하거나 타임아웃 대기한다.
- (3) 준비된 채널의 SelectionKey를 반환한다.
- (4) 애플리케이션이 해당 채널만 처리한다.
- 이 방식으로 단일 스레드가 수만 개의 연결을 효율적으로 처리할 수 있다.
- Netty와 같은 비동기 프레임워크의 핵심 기술이다.
### WatchService
- WatchService는 파일 시스템의 변경사항을 실시간으로 감지하는 API다.
- OS의 파일 시스템 이벤트 알림 메커니즘을 활용한다.
- Linux: inotify - 커널 레벨에서 파일 시스템 이벤트를 추적. 파일 생성/수정/삭제/이동 등 이벤트를 효율적으로 감지 (내부적으로 호출됨)
- Windows: ReadDirectoryChangesW - 디렉토리의 변경사항을 비동기적으로 모니터링하는 Win32 API. 오버랩 I/O를 통해 효율적으로 처리한다. (JNI를 통해 호출됨)
- macOS: FSEvents - 파일 시스템 레벨의 이벤트 스트림을 제공. 타임 스템프와 함께 세밀한 이벤트 정보 전달 (Objective-C 브릿지 통해 호출됨)
### 베스트 프렉티스
- 적절한 추상화 선택: 간단한 작업에는 Files 클래스를, 성능이 중요하면 FileChannel을, 대량 동시 연결에는 NIO Selector를 사용
- 버퍼링 활용: 작은 단위의 I/O는 반드시 버퍼링 - BufferedInputStream/Reader 또는 적절한 크기의 ByteBuffer를 사용해 시스템 콜을 최소화
- 자원 관리: try-with-resources를 사용해 스트림과 채널을 확실히 닫는다. 파일 디스크립터 누수 안 되도록
- 인코딩 명시: 문자 스트림 사용 시 항상 Charset을 명시적으로 지정한다. 플랫폼 기본 인코딩으로 하면 이식성 문제
- 성능 프로파일링: 대용량 파일 처리 시 메모리 맵 파일, Director Buffer, 논블로킹 I/O 등을 고려하되, 실제 측정을 통해 검증한다.