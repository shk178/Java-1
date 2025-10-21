## BufferedStream
- 보조 스트림: 보조 기능 제공
```java
FileOutputStream fos = new FileOutputStream(FILE_NAME);
BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
long sTime = System.currentTimeMillis();
for (int i = 0; i < FILE_SIZE; i++) {
    bos.write(0); // 내부 버퍼에 1바이트씩 저장
    // 내부 버퍼가 차면 fos.write(버퍼) 실행
    /*
    bos.write(0); // 매번 1바이트씩 write() 호출이 되지만,
    // 내부적으로는 BUFFER_SIZE만큼 모아서 fos.write()를 호출
    bos.write(byte[], off, len); // 이렇게 하면 더 빠름
     */
}
bos.close();
/*
bos.close(); 실행 시
bos.flush(); 자동 실행됨 // 남은 데이터 강제 출력, 버퍼가 안 찬 상태에서 fos.write 실행
fos.close(); 자동 실행됨 // bos.flush() 하기 전에 fos.close() 하면 안 된다.
 */
FileInputStream fis = new FileInputStream(FILE_NAME);
BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE);
sTime = System.currentTimeMillis();
int fileSize = 0;
int data;
while ((data = bis.read()) != -1) {
    fileSize++; // 1바이트씩 읽은 횟수 카운트
}
/*
1. 블록 단위로 읽기 (디스크 → 메모리)
BufferedInputStream은 read()가 처음 호출되면
내부적으로 FileInputStream.read(byte[])를 사용해서
BUFFER_SIZE만큼 한 번에 디스크에서 읽어와서 내부 버퍼에 저장
2. 1바이트씩 반환 (메모리 → 사용자 코드)
이후 read()를 호출할 때마다 내부 버퍼에서 1바이트씩 꺼내서 반환
디스크에 다시 접근하지 않고 메모리에서 빠르게 처리
3. 버퍼가 다 소진되면 다시 블록 단위로 읽기
내부 버퍼가 비면 다시 BUFFER_SIZE만큼 디스크에서 읽어와서 버퍼를 채우고
또 1바이트씩 꺼내는 방식
 */
bis.close();
```
- BufferedStream 같은 클래스는 내부적으로 동기화 처리가 되어 있다.
- 멀티스레드 환경에서 안전하게 동작하도록 하기 위해서다.
```java
public synchronized int read() {
    // 내부 버퍼에서 1바이트 읽기
}
```
- read()/write() 메서드가 synchronized로 선언되어 있어 한 번에 하나의 스레드만 접근 가능
- 앞서 직접 버퍼 만든 경우
```java
byte[] buffer = new byte[BUFFER_SIZE];
int index = 0;
for (...) {
    buffer[index++] = data;
    if (index == BUFFER_SIZE) {
        fos.write(buffer);
        index = 0;
    }
}
```
- 동기화가 없기 때문에 단일 스레드에서는 더 빠르게 동작
- 멀티스레드 환경에서는 안전하지 않을 수 있다.
- FileInputStream / FileOutputStream 객체는 공유 자원
- 파일 스트림은 디스크 파일이라는 물리적 자원에 대한 핸들
- BufferedInputStream / BufferedOutputStream은 내부 byte[] 버퍼도 공유됨

| 상황 | 설명 | 안전 여부 |
|------|------|---------|
| 각 스레드가 자기만의 스트림 객체를 갖고 있음 | 파일을 동시에 읽거나 쓰되, 각자 독립된 스트림을 사용 | 안전함 |
| 여러 스레드가 같은 스트림 객체를 공유함 | BufferedOutputStream을 공유하면서 동시에 write() 호출 | 위험함 (동기화 필요) |
| 스트림 객체에 동기화(synchronized) 처리함 | synchronized 블록으로 접근 제어 | 조건부 안전 (성능 저하 가능) |
| FileChannel이나 RandomAccessFile로 명시적 위치 제어 | 각 스레드가 다른 위치에 쓰도록 제어 | 안전함 (고급 방식) |

- 해결 방법
- 동기화 처리: synchronized 키워드나 ReentrantLock으로 스트림 접근을 제어
- 스레드별 스트림 분리: 각 스레드가 독립적으로 FileInputStream 또는 FileOutputStream을 생성
- 고급 API 사용: FileChannel, MappedByteBuffer, AsynchronousFileChannel 등은 멀티스레드에 더 적합
## 입출력(IO) 클래스
- OutputStream/InputStream은 바이트 기반 스트림
- Writer/Reader는 문자 기반 스트림
- 1. 바이트 스트림 계층
- InputStream (추상 클래스)
- FileInputStream, ByteArrayInputStream, BufferedInputStream, ObjectInputStream
- OutputStream (추상 클래스)
- FileOutputStream, ByteArrayOutputStream, BufferedOutputStream, ObjectOutputStream
- 2. 문자 스트림 계층
- Reader (추상 클래스)
- FileReader, BufferedReader, CharArrayReader, StringReader
- InputStreamReader ← InputStream을 Reader로 변환
- Writer (추상 클래스)
- FileWriter, BufferedWriter, CharArrayWriter, StringWriter
- OutputStreamWriter ← OutputStream을 Writer로 변환
- 변환 클래스들은 바이트 스트림을 문자 스트림으로 감싸는 역할
- 기본(기반/메인) 스트림: 단독 사용 가능
- 보조 스트림: 기본 스트림과 함께 사용
### Java는 내부적으로 UTF-16을 사용
- char 타입: 2바이트 UTF-16 코드 유닛
- String 내부: UTF-16으로 인코딩된 char 배열
- 자바 메모리: 모든 문자(char, String)를 UTF-16으로 저장
### 파일은 다양한 인코딩 사용 가능
- 디스크: 파일에 다양한 인코딩으로 문자 저장
- UTF-8 (가변 길이), EUC-KR, ISO-8859-1 등
- 따라서 필요한 것: Java UTF-16 ↔ 파일 인코딩 간의 변환
### OutputStreamWriter - 쓰기 과정
```java
FileOutputStream fos = new FileOutputStream("data.txt");
OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8);
osw.write("안녕");
osw.close();
```
```
[1] Java 메모리 (UTF-16)
┌──────────────────────┐
│ "안녕"               │
│ U+C548 U+B155       │
│ (각 2바이트)         │
└──────────────────────┘
        ↓
    write() 호출
        ↓
[2] 내부 버퍼에 저장
┌──────────────────────┐
│ char[] 버퍼          │
│ 0xC548 0xB155       │
└──────────────────────┘
        ↓
flush() 또는 버퍼 가득 참
        ↓
[3] UTF-16 → UTF-8 인코딩 변환
┌──────────────────────┐
│ CharsetEncoder       │
│ UTF-16 → UTF-8       │
└──────────────────────┘
        ↓
U+C548 ('안') → 0xEC 0x95 0x88 (3바이트)
U+B155 ('녕') → 0xEB 0x85 0x95 (3바이트)
        ↓
[4] 바이트 스트림으로 전달
┌──────────────────────┐
│ FileOutputStream     │
│ 0xEC 0x95 0x88      │
│ 0xEB 0x85 0x95      │
└──────────────────────┘
        ↓
[5] 파일에 기록 (UTF-8)
```
### InputStreamReader - 읽기 과정
```java
FileInputStream fis = new FileInputStream("data.txt");
InputStreamReader isr = new InputStreamReader(fis, UTF_8);
int ch;
while ((ch = isr.read()) != -1) {
    char c = (char) ch;
    System.out.print(c);
}
isr.close();
```
```
[1] 파일 (UTF-8)
┌──────────────────────┐
│ 0xEC 0x95 0x88      │  '안' (3바이트)
│ 0xEB 0x85 0x95      │  '녕' (3바이트)
└──────────────────────┘
        ↓
[2] FileInputStream으로 바이트 읽기
┌──────────────────────┐
│ 바이트 스트림         │
│ 0xEC, 0x95, 0x88... │
└──────────────────────┘
        ↓
[3] UTF-8 패턴 분석 및 바이트 수집
┌──────────────────────┐
│ CharsetDecoder       │
│ 첫 바이트 분석       │
│ 0xEC → 3바이트 필요  │
└──────────────────────┘
        ↓
필요한 바이트 모두 수집
0xEC 0x95 0x88 (3바이트)
        ↓
[4] UTF-8 → UTF-16 디코딩 변환
┌──────────────────────┐
│ UTF-8: 0xEC 0x95 0x88│
│        ↓             │
│ UTF-16: U+C548       │
│ (0xC548, 2바이트)    │
└──────────────────────┘
        ↓
[5] int로 반환 (UTF-16 코드 포인트)
┌──────────────────────┐
│ int: 0x0000C548     │
│ (4바이트, 하위 16비트│
│  만 사용)            │
└──────────────────────┘
        ↓
[6] (char) 캐스팅
┌──────────────────────┐
│ char: 0xC548 = '안' │
│ (2바이트 UTF-16)     │
└──────────────────────┘
```
### UTF-8의 바이트 패턴

| 바이트 수 | 첫 바이트 패턴 | 예시 |
|-----------|----------------|------|
| 1바이트 | 0xxxxxxx | 'A' (0x41) |
| 2바이트 | 110xxxxx | 'ą' (0xC4 0x85) |
| 3바이트 | 1110xxxx | '가' (0xEA 0xB0 0x80) |
| 4바이트 | 11110xxx | '😀' (0xF0 0x9F 0x98 0x80) |

### InputStreamReader의 바이트 수집 로직
```java
// InputStreamReader 내부 동작 (의사 코드)
1. FileInputStream에서 첫 바이트 읽기
byte1 = fis.read(); // 0xEC
2. 첫 바이트 패턴 분석
if (byte1 starts with 1110) {
    // 3바이트 문자
    byte2 = fis.read(); // 0x95
    byte3 = fis.read(); // 0x88
}
3. 수집한 바이트로 UTF-8 디코딩
bytes = [0xEC, 0x95, 0x88]
char = decode(bytes); // U+C548
4. UTF-16으로 변환하여 int 반환
return 0x0000C548;
```
### 인코딩별 바이트 수 비교
```
문자 '가' (U+AC00)
├─ UTF-16 (Java 메모리): 0xAC00 (2바이트)
├─ UTF-8 (파일): 0xEA 0xB0 0x80 (3바이트)
└─ EUC-KR (파일): 0xB0 0xA1 (2바이트)
문자 'A' (U+0041)
├─ UTF-16 (Java 메모리): 0x0041 (2바이트)
├─ UTF-8 (파일): 0x41 (1바이트)
└─ ASCII (파일): 0x41 (1바이트)
문자 '😀' (U+1F600)
├─ UTF-16 (Java 메모리): 0xD83D 0xDE00 (4바이트, 서로게이트 페어)
└─ UTF-8 (파일): 0xF0 0x9F 0x98 0x80 (4바이트)
```

| 인코딩 | 설계 목적 | 특징 |
|-------|-----------|------|
| UTF-16 | 메모리 효율 (대부분 문자 2바이트) | Java, Windows 내부 사용 |
| UTF-8 | 파일 크기 효율 (ASCII는 1바이트) | 웹, 파일 저장에 널리 사용 |
| EUC-KR | 한글 특화 | 한글 2바이트, 영문 1바이트 |

### isr.read()가 int를 반환하는 이유
```java
int ch = isr.read();
```
- 이유 1: EOF 표현
```
char 범위: 0 ~ 65535 (0x0000 ~ 0xFFFF, 양수만)
int 범위: -2147483648 ~ 2147483647
EOF: -1 (파일 끝)
→ char로는 -1을 표현할 수 없음
```
- 이유 2: 전체 유니코드 표현
```
BMP 문자: U+0000 ~ U+FFFF (16비트)
확장 문자: U+10000 ~ U+10FFFF (21비트)
→ int(32비트)로 모든 코드 포인트 표현
```
- 반환 값의 구조
```
isr.read() 반환값
┌─────────────────────────────┐
│      32비트 int             │
├───────────────┬─────────────┤
│ 상위 16비트   │ 하위 16비트 │
│ (보통 0)      │ (UTF-16)    │
└───────────────┴─────────────┘
예: '안' (U+C548)
int: 0x0000C548
     └─ 하위 16비트: 0xC548 (UTF-16)
```
## FileWriter / FileReader
- 파일을 문자 단위로 읽고 쓰는 편의 클래스
- FileWriter = FileOutputStream + OutputStreamWriter
- FileReader = FileInputStream + InputStreamReader
```
Writer (추상)
  └─ OutputStreamWriter
       └─ FileWriter
Reader (추상)
  └─ InputStreamReader
       └─ FileReader
```

| 구분 | FileWriter | OutputStreamWriter |
|------|------------|--------------|
| 역할 | 파일 쓰기 전용 편의 클래스 | 범용 문자 출력 스트림 |
| 인코딩 지정 | Java 11 이후 가능 | 가능 |
| 기본 인코딩 | 시스템 기본 인코딩 | 명시적 지정 필요 |
| 유연성 | 낮음 (파일만) | 높음 (모든 OutputStream) |

| 구분 | FileReader | InputStreamReader |
|------|------------|--------------|
| 역할 | 파일 읽기 전용 편의 클래스 | 범용 문자 입력 스트림 |
| 인코딩 지정 | Java 11 이후 가능 | 가능 |
| 기본 인코딩 | 시스템 기본 인코딩 | 명시적 지정 필요 |
| 유연성 | 낮음 (파일만) | 높음 (모든 InputStream) |

- 시스템 기본 인코딩의 문제점
```java
// Windows에서 작성
FileWriter fw = new FileWriter("data.txt");
fw.write("안녕하세요"); // MS949로 저장됨
fw.close();
// Linux에서 읽기
FileReader fr = new FileReader("data.txt");
// UTF-8로 읽으려고 시도 → 한글 깨짐
// [크로스 플랫폼] Windows에서 작성 (MS949) → Linux에서 읽기 (UTF-8) → 깨짐
// [국제화] 한국 시스템 (MS949) → 영어권 시스템 (ISO-8859-1) → 깨짐
// [웹 서버] 로컬 개발 (MS949) → 리눅스 서버 배포 (UTF-8) → 한글 깨짐
```
### FileWriter
```
FileWriter 생성
    ↓
[내부적으로 생성] FileOutputStream
    ↓
OutputStreamWriter (시스템 기본 인코딩 또는 명시된 인코딩)
    ↓
write("안녕")
    ↓
UTF-16 (Java 메모리)
    ↓
[인코딩 변환]
    ↓
파일에 저장
```
```java
// FileWriter 내부 구조 (개념적)
public class FileWriter extends OutputStreamWriter {
    public FileWriter(String fileName) throws IOException {
        super(new FileOutputStream(fileName));
        // 기본 인코딩 사용
    }
    public FileWriter(String fileName, Charset charset) throws IOException {
        super(new FileOutputStream(fileName), charset);
        // 명시적 인코딩 사용 (Java 11+)
    }
}
```
### FileReader
```
FileReader 생성
    ↓
[내부적으로 생성] FileInputStream
    ↓
InputStreamReader (시스템 기본 인코딩 또는 명시된 인코딩)
    ↓
read()
    ↓
파일에서 바이트 읽기
    ↓
[디코딩 변환]
    ↓
UTF-16 (Java 메모리)
    ↓
int 반환
```
```java
// FileReader 내부 구조 (개념적)
public class FileReader extends InputStreamReader {
    public FileReader(String fileName) throws IOException {
        super(new FileInputStream(fileName));
        // 기본 인코딩 사용
    }
    public FileReader(String fileName, Charset charset) throws IOException {
        super(new FileInputStream(fileName), charset);
        // 명시적 인코딩 사용 (Java 11+)
    }
}
```
### 왜 버퍼링이 필요한가
- 버퍼 없이 (느림): FileWriter.write() 호출마다 → 디스크 접근
- 버퍼 사용 (빠름): BufferedWriter → 8KB 모았다가 → 한 번에 디스크 접근
```java
// FileWriter + BufferedWriter
try (BufferedWriter bw = new BufferedWriter(
        new FileWriter("data.txt", StandardCharsets.UTF_8))) {
    bw.write("첫 번째 줄\n");
    bw.write("두 번째 줄\n");
    // 버퍼에 모았다가 한 번에 쓰기
}
// FileReader + BufferedReader
try (BufferedReader br = new BufferedReader(
        new FileReader("data.txt", StandardCharsets.UTF_8))) {
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
}
// OutputStreamWriter + BufferedWriter
try (BufferedWriter bw = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream("data.txt"), StandardCharsets.UTF_8))) {
    bw.write("안녕하세요\n");
}
// InputStreamReader + BufferedReader
try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream("data.txt"), StandardCharsets.UTF_8))) {
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
}
```