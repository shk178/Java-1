- 8 bit = 0~255, -128~127
- 문자 인코딩: 문자 집합을 통해 문자를 숫자로 변환
- 문자 디코딩: 문자 집합을 통해 숫자를 문자로 변환
- 유니코드: UTF-16 (2byte), UTF-8 (1-4byte)
- 자바: UTF-16 (char 메모리 저장 시) -> (영문 등은 byte로 바꿔 저장)
```
논리형	boolean	1byte	true 또는 false 값
문자형	char	2byte	유니코드 문자를 저장
정수형	byte	1byte	-128 ~ 127
short	2byte	-32,768 ~ 32,767
int	4byte	가장 일반적으로 사용되는 정수형
long	8byte	매우 큰 정수값을 저장
실수형	float	4byte	단정밀도 실수
double	8byte	배정밀도 실수, float보다 더 큰 범위와 정밀도
```
- 자바 음수: 2의 보수
```
2의 보수는 양수의 이진수 표현을 뒤집고(1의 보수), 거기에 1을 더한 것
컴퓨터가 덧셈만으로 음수 연산을 처리할 수 있다.
예를 들어, 8비트 기준으로 +5와 -5를 표현하면:
- +5 → 00000101
- -5 → 11111011 (2의 보수)
계산 방법 (8비트 기준)
1. 양수 이진수로 변환 // 5 → 00000101
2. 1의 보수 계산 (비트 반전) // 00000101 → 11111010
3. 1 더하기 (2의 보수) // 11111010 + 1 → 11111011
11111011이 -5의 2의 보수 표현
왜 2의 보수를 쓰는 걸까?
- 덧셈과 뺄셈을 같은 회로로 처리 가능
- 0이 유일하게 표현됨 (1의 보수는 +0과 -0이 있음)
- 오버플로우 처리가 간단함
```
- Byte에는 toBinaryString() 메서드가 없다.
- byte는 보통 int로 자동 변환해서 처리됨
- 이진 표현이 필요한 경우는 대부분 int 이상에서 발생
- 인코딩 디코딩 특정 Charset으로: 지원하는 경우만 가능
- 인코딩 디코딩 다른 Charset으로: (확장 등) 호환되는 경우만 가능
- `public int read() throws IOException`
- InputStream.read()는 한 바이트를 읽는다.
- 그런데 왜 int를 반환할까
```java
// 파일의 내용 (바이트 단위):  [ 0x00 (00000000=0) ] [ 0x7F (01111111=127) ] [ 0xFF (11111111=255) ]
InputStream in = new FileInputStream("data.bin");
int b1 = in.read(); // 0x00  -> 00000000 00000000 00000000 00000000
int b2 = in.read(); // 0x7F  -> 00000000 00000000 00000000 01111111
int b3 = in.read(); // 0xFF  -> 00000000 00000000 00000000 11111111
// 파일에서 읽은 8비트를 int의 하위 8비트에만 채워 넣고
// 나머지 24비트는 0으로 채워서 표현
// 파일은 여전히 8비트씩 읽고, 자바는 처리할 때만 32비트 사용
```
- 자바의 byte 타입은 -128~127까지만 표현할 수 있다.
- 만약 0xFF(255)를 byte로 읽으면 → -1로 잘못 해석된다.
- 파일에서 읽은 바이트의 실제 값을 유지하기 위해 int로 표현하는 우회 방법을 쓴다.
- 덤으로 -1을 EOF 용으로 쓸 수 있어서 일석이조다.
- `public void write(int b) throws IOException`
- OutputStream.write(int)는 한 바이트를 파일(또는 네트워크 등) 에 쓴다.
- 한 바이트만 쓰는데 int를 받는 이유
- 자바의 byte는 -128~127 범위이기 때문
- int를 받지만 실제로는 맨 아래 8비트만 파일에 기록
```java
OutputStream out = new FileOutputStream("data.bin");
// 아래 코드는 모두 같은 의미
out.write(0); // 00000000 → 0x00
out.write(127); // 01111111 → 0x7F
out.write(255); // 11111111 → 0xFF
out.write(511); // 00000000 00000001 11111111 → 0xFF (하위 8비트만)
// 한 번에 여러 바이트를 쓰는 메서드도 있다.
out.write(byte[] b);
// 1. 배열의 각 요소(byte값)를 0~255로 변환한다.
// (byte(signed) 값을 0~255 범위의 정수(int)로 바꾼다는 뜻)
// 2. 하위 8비트만 파일에 쓴다.
for (int i = 0; i < b.length; i++) {
    write(b[i] & 0xFF); // 하위 8비트만 파일에 씀
}
// 자바 byte → int 변환 시 “비트가 그대로 올라간다”
// 비트를 복사해서 32비트로 확장(sign extension)한다.
// 단순 캐스팅만으로는 부호가 유지된다.
byte b = (byte)0xFF; // 11111111 → 0xFF인데 -1로 해석
int i = b; // 11111111 11111111 11111111 11111111 → -1
// 그런데 우리는 “이 비트를 0~255로 해석하려 한다”
// 그래서 이렇게 부호 확장을 막고 하위 8비트만 유지한다.
int value = b & 0xFF; // 11111111 & 11111111 → 255
// 자바에서 -1(byte)은 비트로 11111111로 저장된다.
// 파일에는 이 비트 그대로 저장되므로 11111111 = 0xFF = 255로 해석된다.
// 즉, 파일에서는 -0x01 같은 개념이 없고, -1로 해석하지 않는다.
// 자바는 byte가 signed(-128~127)이므로,
// 바이트를 읽고 쓸 때 부호 문제를 피하려고 read()/write()가 int를 사용한다.
```
- read()와 write()의 두 가지 방식
- 1. 단일 바이트 처리 (int 사용)
```java
int data = fis.read(); // 한 바이트 읽기
fos.write(65); // 한 바이트 쓰기 (예: 'A')
```
- read()는 한 바이트씩 읽어서 int로 반환함 (0~255 또는 -1)
- write(int b)는 한 바이트를 파일에 씀
- 주로 간단한 테스트나 루프에서 한 바이트씩 처리할 때 사용
- 2. 배열 처리 (byte[] 사용)
```java
byte[] buffer = new byte[100];
int count = fis.read(buffer); // 여러 바이트 읽기
fos.write(buffer); // 여러 바이트 쓰기
```
- read(byte[] b)는 여러 바이트를 한 번에 읽음
- write(byte[] b)는 배열 전체를 파일에 씀
- 성능이 더 좋고, 대용량 파일 처리에 적합
- 왜 read()는 int를 반환할까
- read()는 파일 끝(EOF)을 구분하기 위해 -1을 반환할 수 있어야 한다.
- byte는 부호 있는 타입이라 -1을 표현할 수 있지만, 읽은 바이트 값과 구분하기 위해 int로 반환
- 부분으로 나누어 읽기: `read(byte[], offset, length)`
- 대용량 파일 읽을 때, 한 번에 메모리에 로드하기 보다는
- 부분으로 나눠 읽어서 메모리 사용량을 제어할 수 있다.
- 전체 읽기: `readAllBytes()`
```java
public static void main(String[] args) throws IOException {
    FileOutputStream fos = new FileOutputStream("temp/hello2.dat");
    byte[] input = {65, 66, 67}; // ASCII 값: A, B, C
    fos.write(input); // 파일에 A, B, C 저장
    fos.close();
    FileInputStream fis = new FileInputStream("temp/hello2.dat");
    byte[] buffer = new byte[10]; // 10바이트 크기의 버퍼
    int readCount = fis.read(buffer, 0, 10); // 최대 10바이트 읽기
    // buffer: 바이트 배열 (바이트 단위로 쓰거나 읽어서 int 배열은 안 된다.)
    // off: 오프셋 - 데이터를 저장할 시작 위치가 byteArr[off]
    // len: 최대 읽을 바이트 수 - 최대 len바이트 읽기 시도
    System.out.println(readCount); // 3
    System.out.println(Arrays.toString(buffer)); // [65, 66, 67, 0, 0, 0, 0, 0, 0, 0]
    // 파일의 끝(EOF)에 도달
    // reset()은 InputStream 클래스에 정의되어 있지만
    // FileInputStream은 이를 오버라이드하지 않아서 지원하지 않음
    // BufferedInputStream 같은 일부 스트림은 내부 버퍼를 사용해서 mark()와 reset()을 지원함
    fis.close(); // 리소스 해제 (파일 핸들 닫기) - 여러 스트림이면 각각 해제 필요
    // 객체를 close()했다고 해서 즉시 메모리에서 정리되지 않음
    // close()는 리소스를 해제하는 역할
    // 객체의 메모리 정리(Garbage Collection)는 JVM이 따로 관리
    fis = new FileInputStream("temp/hello2.dat");
    // fis는 참조 변수 (stack에 저장됨)
    // 이전 객체는 더 이상 참조되지 않아서 GC 대상이 됨
    byte[] buffer2 = fis.readAllBytes();
    System.out.println(Arrays.toString(buffer2)); // [65, 66, 67]
    fis.close();
}
```
- `readAllBytes()`를 사용할 때 OutOfMemoryError가 발생하는 시점
- JVM이 더 이상 바이트 배열을 힙 메모리에 할당할 수 없을 때
- readAllBytes()는 파일의 전체 내용을 한 번에 메모리로 읽어들여 byte[] 배열로 반환
- 내부적으로 `new byte[fileSize]`를 시도하기 때문에, 파일 크기가 크면 힙 메모리에 큰 배열을 할당해야 함
- 만약 JVM이 그만큼의 메모리를 확보할 수 없다면 OutOfMemoryError가 발생

| 상황 | 설명 |
|------|------|
| 파일 크기가 수백 MB~GB 이상일 때 | readAllBytes()는 한 번에 다 읽으므로 위험함 |
| JVM 힙 메모리 제한이 작을 때 | 기본은 256MB~1GB 정도일 수 있음 (설정에 따라 다름) |
| 다른 객체들이 이미 메모리를 많이 쓰고 있을 때 | 힙이 꽉 차 있으면 작은 파일도 위험할 수 있음 |

```java
byte[] data = Files.readAllBytes(Paths.get("huge_video.mp4")); // 2GB 파일
// JVM이 1GB 힙만 허용한다면 OutOfMemoryError가 발생
```
- 안전하게 처리하는 방법
- 대용량 파일은 read() + 버퍼로 나눠서 읽기
```java
// 대용량 파일은 스트리밍 방식으로 읽는 게 안전
try (InputStream is = new FileInputStream("large.dat")) {
    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = is.read(buffer)) != -1) {
        // 처리 로직
    }
}
```
- JVM 옵션으로 힙 메모리 늘리기
```bash
java -Xmx2G MyProgram // 최대 2GB까지 힙을 사용할 수 있다.
```
## InputStream, OutputStream
- 자바는 (파일, 네트워크, 콘솔) I/O를 byte 단위로 한다.
- InputStream/OutputStream이라는 추상 클래스를 제공한다.
- InputStream - read(), read(byte[]), readAllBytes()
- 상속 클래스 - FileInputStream, ByteArrayInputStream, SocketInputStream
- OutputStream - write(int), write(byte[])
- 상속 클래스 - FileOutputStream, ByteArrayOutputStream, SocketOutputStream
```java
public class ByteArrayStream {
    public static void main(String[] args) throws IOException {
        byte[] input = {65, 66, 67};
        // 메모리상에 데이터를 저장하는 출력 스트림
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(input);
        // bos.toByteArray()는 ByteArrayOutputStream에 저장된 데이터를 byte[]로 반환
        // bis는 이 바이트 배열을 읽는 입력 스트림
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        byte[] bytes = bis.readAllBytes();
        System.out.println(Arrays.toString(bytes)); // [65, 66, 67]
        bos.close();
        bis.close();
    }
}
// ByteArrayOutputStream은 파일처럼 동작하는 메모리 기반의 출력 스트림
// 실제 파일을 쓰는 대신, 메모리 안의 바이트 배열에 데이터를 저장
// FileOutputStream은 디스크에 저장, ByteArrayOutputStream은 메모리에 저장
// 파일 없이도 스트림 기반 로직을 테스트하거나 처리 가능
```
- 버퍼링(Buffering)이란
- 데이터를 한 번에 조금씩 처리하지 않고, 일정량을 모아서 한 번에 처리하는 방식
- 목적: 입출력 성능 향상
- 디스크에서 1바이트씩 읽기: 느림 (I/O 호출이 많음)
- 8KB씩 한 번에 읽기: 빠름 (I/O 호출 횟수 감소)
- FileInputStream (파일 기반)
```java
FileInputStream fis = new FileInputStream("data.txt");
// fis.mark()와 fis.reset()은 지원되지 않음
```
- 운영체제의 파일 핸들을 통해 데이터를 읽음
- 내부적으로 파일 포인터를 순차적으로 움직임
- mark()/reset() 기능이 구현되어 있지 않음
- 되돌리려면 파일을 다시 열어야 함
- ByteArrayInputStream (메모리 기반)
```java
ByteArrayInputStream bis = new ByteArrayInputStream(new byte[]{65, 66, 67});
bis.mark(0); // 현재 위치 저장
bis.read(); // 65
bis.read(); // 66
bis.reset(); // 다시 65로 돌아감
```
- 바이트 배열을 메모리에서 직접 읽음
- 배열 인덱스로 위치를 관리
- mark()/reset() 지원 (인덱스를 저장하고 되돌림)
- 메모리 기반이라 매우 빠름
- 내부 배열 자체가 버퍼 역할
- BufferedInputStream (버퍼링 추가)
```java
BufferedInputStream bis = new BufferedInputStream(new FileInputStream("data.txt"));
int b;
while ((b = bis.read()) != -1) {
    System.out.print((char) b);
}
// read() 호출 시, 버퍼가 비어있으면 파일에서 8KB를 한 번에 읽어옴
// 이후 read() 호출은 버퍼에서 데이터를 꺼내기만 함
// 버퍼가 다시 비면 다음 8KB를 읽어옴
bis.mark(100); // 최대 100바이트까지 되돌릴 수 있음
bis.read();
bis.reset(); // mark 위치로 되돌리기
```
- 기존 InputStream에 버퍼링 기능을 추가하는 래퍼(wrapper) 클래스
- 내부에 8KB 크기의 바이트 배열을 버퍼로 사용
- 파일에서 데이터를 미리 읽어서 버퍼에 저장 → 이후 요청 시 버퍼에서 빠르게 제공
- mark()/reset() 지원
- I/O 호출 횟수를 줄여 성능 대폭 향상
- RandomAccessFile (임의 위치 접근)
```java
RandomAccessFile raf = new RandomAccessFile("data.txt", "rw");
raf.seek(4); // 5번째 바이트로 이동
raf.writeByte(100); // 해당 위치에 쓰기
raf.seek(0); // 처음으로 이동
int b = raf.readByte();
// 대용량 파일에서 특정 위치만 수정
// 고정 길이 레코드 기반 데이터 처리
// 바이너리 파일, 로그 파일 다루기
// 파일을 여러 번 읽고 써야 할 때
```
- 스트림이 아닌 파일 핸들 기반 클래스
- 파일의 임의 위치로 이동 가능 (seek() 메서드)
- "r" 읽기 전용, "rw" 읽기/쓰기 가능
- 버퍼링이 내장되어 있지 않음
- 성능이 필요하면 직접 버퍼를 관리하거나 BufferedReader와 함께 사용
- mark()/reset()이 되는 이유
- 메모리 기반 (ByteArrayInputStream): 배열 인덱스를 저장하고 되돌리면 됨
- 버퍼 기반 (BufferedInputStream): 버퍼에 데이터가 있어서 되돌리기 가능
- mark()/reset()이 안 되는 이유
- 파일 스트림 (FileInputStream): 파일 포인터는 순차적으로만 움직임, 되돌리기 기능 없음
- RandomAccessFile: seek()로 직접 위치 이동 가능하지만, mark/reset은 지원 안 함
- 성능 최적화
- 작은 단위로 자주 읽기 → BufferedInputStream으로 감싸기
- 임의 위치 접근 필요 → RandomAccessFile 사용
- 메모리에서만 작업 → ByteArrayInputStream 사용
- PrintStream
```java
PrintStream printStream = System.out;
byte[] bytes = "Hello!\n".getBytes(StandardCharsets.UTF_8);
printStream.write(bytes); // Hello!
printStream.println("Print!"); // Print!
```
- write(byte[])는 OutputStream 기능 오버라이딩이고
- println(String)은 PrintStream 추가 메서드다.
## FileOutput/InputStream
- 251020-java-adv2/src/iobuffered/CreateFile.java
- 1바이트씩 쓰고 읽음 - 매번 시스템 콜 발생
- 251020-java-adv2/src/iobuffered/CreateFile2.java
- 버퍼링의 목적
- 디스크는 메모리보다 훨씬 느리기 때문에, 작은 단위로 자주 쓰는 것보다 큰 블록으로 묶어서 쓰는 것이 효율적
- 버퍼의 역할
- 데이터를 일정 크기만큼 모아두는 임시 저장소
- 버퍼가 가득 차면 한 번에 디스크에 전달
- 디스크 접근 횟수 감소 → 시스템 콜과 I/O 오버헤드 감소
- 일반적인 경우: 8KB~16KB 버퍼 권장
- 대용량 파일: 32KB 이상 고려
- 작은 파일: 4KB~8KB로 충분

| 버퍼 크기 | 특징 |
|---------|------|
| 4KB | OS의 기본 페이지 크기와 일치, 많은 시스템에서 기본 I/O 단위, 메모리 사용량 감소, 빠른 응답성 |
| 8KB 이상 | 더 많은 데이터를 한 번에 처리, 쓰기 성능 향상 가능, 시스템 콜 횟수 감소 → 오버헤드 감소 |
| 너무 큰 버퍼 | 메모리 낭비, 작은 파일 처리 시 비효율적 |

| 디스크 종류 | 버퍼 사이즈 영향 | 이유 |
|-------------|------------------|------|
| HDD (하드디스크) | 큰 버퍼가 유리 | 디스크 회전과 헤드 이동이 느림 |
| SSD (솔리드 스테이트) | 작은 버퍼도 괜찮음 | 랜덤 액세스가 빠름 |
| RAM 디스크 | 거의 영향 없음 | 메모리 기반이라 매우 빠름 |

- I/O 처리의 3단계 구조
- 1. 사용자 버퍼: I/O 요청 빈도 조절
- 2. 시스템 콜: 사용자 지정 크기만큼 요청
- 3. OS 페이지 캐시: 실제 메모리/디스크 관리 (4KB 단위)
```
[사용자 프로그램]
    ↓
버퍼 (8KB) ← 사용자 수준의 I/O 단위
    ↓
[시스템 콜]
    ↓
OS 페이지 캐시 (4KB) ← OS 수준의 메모리 관리 단위
    ↓
[디스크]
```
- 사용자 수준 - 버퍼 사이즈
```java
FileOutputStream fos = new FileOutputStream("data.txt");
BufferedOutputStream bos = new BufferedOutputStream(fos, 8192); // 8KB 버퍼
```
- 프로그램이 데이터를 얼마나 모아서 OS에 넘길지 결정
- 버퍼가 8KB면, 8KB가 찰 때까지 모았다가 한 번에 OS에 전달
- I/O 요청의 빈도와 크기를 조절
- 시스템 콜 수준
```c
write(fd, buf, 6000); // 6000바이트를 쓰도록 요청
```
- 시스템 콜은 사용자가 지정한 바이트 수만큼 처리하도록 요청
- 페이지 단위로 요청하는 것이 아님
- OS에게 이 크기만큼 처리해줘라고 요청하는 것
- OS 수준 - 페이지 캐시
- OS는 메모리를 4KB 페이지 단위로 관리
- 디스크 I/O도 페이지 캐시를 통해 최적화
```
사용자: write(fd, buf, 6000) 요청
    ↓
OS: 6000바이트를 받아서 페이지 단위로 분할
    ↓
페이지 캐시: 4KB + 2KB로 나눠서 저장
    ↓
디스크: 페이지 단위로 플러시 (flush)
```

| 구분 | 버퍼 사이즈 | 페이지 크기 |
|------|-------------|-------------|
| 제어 주체 | 사용자 프로그램 | 운영체제 |
| 목적 | I/O 요청 빈도 조절 | 메모리/디스크 관리 |
| 단위 | 자유롭게 설정 가능 (1KB, 8KB 등) | 고정 (보통 4KB) |
| 역할 | 시스템 콜 호출 횟수 감소 | 실제 디스크 I/O 최적화 |

- 예시 1: 버퍼 8KB, 데이터 20KB 쓰기
```
사용자 프로그램:
    write() 20번 호출 (각 1KB) → 버퍼에 쌓임
버퍼 (8KB):
    8KB 차면 → 시스템 콜 호출
    8KB 차면 → 시스템 콜 호출
    4KB 차면 → 시스템 콜 호출
    = 총 3번의 시스템 콜
OS (페이지 캐시 4KB):
    8KB → 4KB + 4KB 페이지로 분할
    8KB → 4KB + 4KB 페이지로 분할
    4KB → 4KB 페이지
    = 총 5개의 페이지로 처리
디스크:
    5개의 페이지를 디스크에 씀
```
- 예시 2: 버퍼 없이 1KB씩 20번 쓰기
```
사용자 프로그램:
    write() 20번 호출 (각 1KB)
    = 총 20번의 시스템 콜
OS (페이지 캐시 4KB):
    각 1KB 요청을 페이지 캐시에 쌓음
    = 최소 5개의 페이지 사용
디스크:
    OS가 적절한 시점에 페이지 플러시
```