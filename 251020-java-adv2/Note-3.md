## BufferedWriter
```java
BufferedWriter bw = new BufferedWriter(
    new OutputStreamWriter(
        new FileOutputStream("data.txt"),
        StandardCharsets.UTF_8
    )
);
bw.write("안녕하세요");
bw.close();
```
```
[계층 구조]
BufferedWriter (8KB 버퍼)
    ↓
OutputStreamWriter (문자→바이트 변환)
    ↓
FileOutputStream (바이트→파일)
    ↓
운영체제 (페이지 캐시 4KB)
    ↓
디스크
```
### write() 호출 시 흐름
```
[1단계] 사용자 코드
┌─────────────────────────────┐
│ bw.write("안녕하세요");     │
│ (5문자, UTF-16 기준 10바이트)│
└─────────────────────────────┘
        ↓
[2단계] BufferedWriter 내부 버퍼
┌─────────────────────────────┐
│ char[] cb = new char[8192]; │  ← 기본 8KB (8192 문자)
│ int nChars = 0;             │  ← 현재 채워진 문자 수
│                             │
│ "안녕하세요" → 버퍼에 저장  │
│ nChars = 5                  │
│ [안][녕][하][세][요][][]... │
└─────────────────────────────┘
        ↓
    버퍼 가득 찼나?
        ↓
    ┌───┴───┐
   NO      YES
    ↓       ↓
  대기    flush()
[3단계] flush() 또는 버퍼가 가득 차면
┌─────────────────────────────┐
│ OutputStreamWriter에 전달   │
│ write(cb, 0, nChars)        │
└─────────────────────────────┘
        ↓
[4단계] 문자 → 바이트 인코딩
┌─────────────────────────────┐
│ CharsetEncoder (UTF-8)      │
│ '안' → 0xEC 0x95 0x88      │
│ '녕' → 0xEB 0x85 0x95      │
│ '하' → 0xED 0x95 0x98      │
│ '세' → 0xEC 0x84 0xB8      │
│ '요' → 0xEC 0x9A 0x94      │
│ = 총 15바이트               │
└─────────────────────────────┘
        ↓
[5단계] FileOutputStream
┌─────────────────────────────┐
│ 15바이트를 파일에 쓰기 요청 │
│ write(byte[] b, 0, 15)      │
└─────────────────────────────┘
        ↓
[6단계] 운영체제
┌─────────────────────────────┐
│ 페이지 캐시 (4KB 단위)      │
│ 15바이트를 페이지에 저장    │
└─────────────────────────────┘
        ↓
[7단계] 디스크
┌─────────────────────────────┐
│ 실제 파일에 기록            │
└─────────────────────────────┘
```
### 여러 번 write() 호출 시
```java
BufferedWriter bw = new BufferedWriter(
    new OutputStreamWriter(
        new FileOutputStream("data.txt"),
        StandardCharsets.UTF_8
    )
);
bw.write("첫 번째 줄\n");   // 6문자
bw.write("두 번째 줄\n");   // 6문자
bw.write("세 번째 줄\n");   // 6문자
bw.close();
```
```
[첫 번째 write()]
"첫 번째 줄\n" (6문자)
    ↓
버퍼 [첫][번][째][ ][줄][\n][][]... (6/8192 차있음)
    ↓
버퍼가 안 찼으니 → 대기 (디스크 접근 없음)
[두 번째 write()]
"두 번째 줄\n" (6문자)
    ↓
버퍼 [첫][번][째][ ][줄][\n][두][번][째][ ][줄][\n]... (12/8192 차있음)
    ↓
버퍼가 안 찼으니 → 대기 (디스크 접근 없음)
[세 번째 write()]
"세 번째 줄\n" (6문자)
    ↓
버퍼 [첫][번][째][ ][줄][\n][두][번][째][ ][줄][\n][세][번][째][ ][줄][\n]... (18/8192 차있음)
    ↓
버퍼가 안 찼으니 → 대기 (디스크 접근 없음)
[close() 호출]
    ↓
남은 버퍼 내용 flush()
    ↓
18문자 → OutputStreamWriter
    ↓
UTF-8 인코딩 (약 54바이트)
    ↓
FileOutputStream
    ↓
운영체제 페이지 캐시
    ↓
디스크에 한 번에 쓰기 (1번의 I/O)
```
- 결과: 3번의 write() 호출 → 1번의 실제 디스크 I/O
## BufferedReader
```java
BufferedReader br = new BufferedReader(
    new InputStreamReader(
        new FileInputStream("data.txt"),
        StandardCharsets.UTF_8
    )
);
String line = br.readLine();
br.close();
```
```
[계층 구조]
디스크
    ↓
운영체제 (페이지 캐시 4KB)
    ↓
FileInputStream (파일→바이트)
    ↓
InputStreamReader (바이트→문자 변환)
    ↓
BufferedReader (8KB 버퍼)
    ↓
사용자 코드
```
### 첫 read() 호출 시
```
[1단계] 사용자 코드
┌─────────────────────────────┐
│ br.readLine()               │
└─────────────────────────────┘
        ↓
[2단계] BufferedReader 버퍼 확인
┌─────────────────────────────┐
│ char[] cb = new char[8192]; │  ← 기본 8KB (8192 문자)
│ 버퍼가 비어있나?            │
└─────────────────────────────┘
        ↓
      YES
        ↓
[3단계] 버퍼 채우기 (fill)
┌─────────────────────────────┐
│ InputStreamReader에 요청    │
│ read(cb, 0, 8192)           │
│ "8192문자를 읽어줘"         │
└─────────────────────────────┘
        ↓
[4단계] InputStreamReader
┌─────────────────────────────┐
│ FileInputStream에서 바이트  │
│ 읽기 시작                   │
└─────────────────────────────┘
        ↓
[5단계] FileInputStream
┌─────────────────────────────┐
│ 파일에서 바이트 읽기        │
│ 예: 100바이트 읽음          │
└─────────────────────────────┘
        ↓
[6단계] UTF-8 → UTF-16 디코딩
┌─────────────────────────────┐
│ InputStreamReader           │
│ 0xEC 0x95 0x88 → '안'       │
│ 0xEB 0x85 0x95 → '녕'       │
│ ...                         │
│ 100바이트 → 약 33문자       │
└─────────────────────────────┘
        ↓
[7단계] BufferedReader 버퍼에 저장
┌─────────────────────────────┐
│ cb[0] = '안'                │
│ cb[1] = '녕'                │
│ cb[2] = '하'                │
│ ...                         │
│ cb[32] = '\n'               │
│ nChars = 33 (33문자 저장됨) │
└─────────────────────────────┘
        ↓
[8단계] readLine() 처리
┌─────────────────────────────┐
│ 버퍼에서 '\n'까지 찾기      │
│ "안녕하세요" 반환           │
│ nextChar = 6 (다음 읽을 위치)│
└─────────────────────────────┘
```
### 두 번째 readLine() 호출 시
```
[1단계] 사용자 코드
┌─────────────────────────────┐
│ br.readLine()               │
└─────────────────────────────┘
        ↓
[2단계] BufferedReader 버퍼 확인
┌─────────────────────────────┐
│ 버퍼에 데이터 남아있나?     │
│ nextChar = 6, nChars = 33   │
│ YES (27문자 남음)          │
└─────────────────────────────┘
        ↓
[3단계] 버퍼에서 직접 읽기
┌─────────────────────────────┐
│ cb[6]부터 '\n'까지 찾기     │
│ "두 번째 줄" 반환           │
│ nextChar = 12               │
│ (디스크 접근 없음)         │
└─────────────────────────────┘
```
- 버퍼에 데이터가 남아있으면 디스크 접근 없이 메모리에서 바로 읽음
### 버퍼 없이 (FileReader 직접 사용)
```java
FileReader fr = new FileReader("data.txt", StandardCharsets.UTF_8);
int ch;
while ((ch = fr.read()) != -1) { // 문자 하나씩 읽기
    System.out.print((char) ch);
}
```
```
read() 1번 호출
    ↓
InputStreamReader → FileInputStream → 디스크
(매번 시스템 콜)
총 1000문자 읽기 = 1000번의 시스템 콜
```
### BufferedReader 사용
```java
BufferedReader br = new BufferedReader(
    new FileReader("data.txt", StandardCharsets.UTF_8)
);
int ch;
while ((ch = br.read()) != -1) { // 문자 하나씩 읽기
    System.out.print((char) ch);
}
```
```
첫 read() 호출
    ↓
버퍼 비어있음
    ↓
8192문자를 한 번에 읽어서 버퍼에 저장
    ↓
이후 read()는 버퍼에서 꺼내기만 함
총 1000문자 읽기 = 1번의 시스템 콜 (빠름)
```
### flush()
```java
BufferedWriter bw = new BufferedWriter(
    new FileWriter("data.txt", StandardCharsets.UTF_8)
);
bw.write("첫 줄\n");
bw.flush(); // 버퍼 → 파일로 강제 쓰기
bw.write("둘째 줄\n");
bw.flush(); // 버퍼 → 파일로 강제 쓰기
bw.close(); // 버퍼 flush + 스트림 닫기
```
```
write("첫 줄\n")
    ↓
버퍼 [첫][ ][줄][\n]... (4/8192)
    ↓
flush() 호출
    ↓
버퍼 내용을 OutputStreamWriter로 전달
    ↓
파일에 쓰기
    ↓
버퍼 초기화 (nChars = 0)
write("둘째 줄\n")
    ↓
버퍼 [둘][째][ ][줄][\n]... (5/8192)
    ↓
flush() 호출
    ↓
버퍼 내용을 OutputStreamWriter로 전달
    ↓
파일에 쓰기
```
### close()
```java
BufferedWriter bw = new BufferedWriter(
    new FileWriter("data.txt", StandardCharsets.UTF_8)
);
bw.write("데이터");
bw.close(); // flush() + 스트림 닫기
```
```
close() 호출
    ↓
[1] flush() 실행
    ↓ 버퍼에 남은 데이터 모두 쓰기
    ↓
[2] OutputStreamWriter.close()
    ↓
[3] FileOutputStream.close()
    ↓
[4] 파일 핸들 해제
```
- 251020-java-adv2/src/iotext/TextStream4.java // FileWriter + BufferedWriter
- 251020-java-adv2/src/iotext/TextStream5.java // FileOutputStream + PrintStream
- 251020-java-adv2/src/iotext/TextStream6.java // FileOutputStream + DataOutputStream
- 251020-java-adv2/src/ioex/Member.java // FileWriter + BufferedWriter
- 251020-java-adv2/src/ioex/Member1.java // FileOutputStream + DataOutputStream

| 구분 | DataOutputStream (이진 저장) | FileWriter/PrintWriter (문자 저장) |
| -- | -------- | -------- |
| 저장 형식 | 0과 1의 바이너리 데이터 | 문자열(텍스트) |
| 저장 용량 | 작을 수 있음 (숫자는 4바이트 고정 등) | 더 큼 (숫자도 문자열로 저장됨) |
| 읽기 속도 | 빠름 (바로 읽어서 타입 변환 불필요) | 느림 (문자 → 숫자 변환 필요) |
| 가독성 | 낮음 (파일 열면 깨져 보임) | 높음 (사람이 읽을 수 있음) |
| 메모리 사용량 | 거의 차이 없음 | 거의 차이 없음 |
| 적합한 상황 | 데이터 처리 중심(프로그램 내부 사용) | 로그, 설정파일 등 사람이 봐야 할 때 |

| 데이터 | DataOutputStream 저장 바이트 | 데이터 | FileWriter 저장 바이트 (UTF-8 기준) |
| --- | ---- | --- | ---- |
| int 25 | 4바이트 | "25" | 2바이트 |
| double 3.14 | 8바이트 | "3.14" | 4바이트 |
| UTF "abc" | 2 + 3 = 5바이트 (길이 + 내용) | "abc" | 3바이트 |

- 251020-java-adv2/src/ioex/Member2.java // ObjectStream
- Serializable 인터페이스: 구현할 기능이 없고 표시가 목적인 마커 인터페이스
## ObjectOutputStream 헤더
### 잘못된 방식
```java
// 첫 번째 저장
ObjectOutputStream oos1 = new ObjectOutputStream(
    new FileOutputStream("members.dat")
);
oos1.writeObject(member1);
oos1.close();
// 두 번째 저장 (append=true)
ObjectOutputStream oos2 = new ObjectOutputStream(
    new FileOutputStream("members.dat", true) // append
);
oos2.writeObject(member2); // 여기서 또 헤더가 추가됨
// [헤더1][member1 데이터][헤더2][member2 데이터]
oos2.close();
// ObjectInputStream으로 읽을 때 StreamCorruptedException 발생
```
### 방법: 전체를 다시 저장
```java
// 1단계: 기존 데이터 읽기
List<Member> members = new ArrayList<>();
File file = new File("members.dat");
if (file.exists() && file.length() > 0) {
    try (ObjectInputStream ois = new ObjectInputStream(
            new FileInputStream(file))) {
        while (true) {
            try {
                members.add((Member) ois.readObject());
            } catch (EOFException e) {
                break; // 더 이상 읽을 게 없으면 종료
            }
        }
    }
}
// 2단계: 새 회원 추가
members.add(newMember);
// 3단계: 전체를 다시 저장 (헤더는 딱 한 번만)
try (ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream(file))) {
    for (Member m : members) {
        oos.writeObject(m);
    }
} // [헤더][member1][member2][member3]...
```
### 방법: 커스텀 ObjectOutputStream (append가 필요할 때)
```java
class AppendableObjectOutputStream extends ObjectOutputStream {
    public AppendableObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }
    @Override
    protected void writeStreamHeader() throws IOException {
        reset(); // 헤더를 쓰지 않고 캐시만 리셋
    }
}
```
```java
File file = new File("members.dat");
FileOutputStream fos = new FileOutputStream(file, true);
ObjectOutputStream oos;
if (file.length() == 0) {
    // 파일이 비어있으면 일반 ObjectOutputStream (헤더 포함)
    oos = new ObjectOutputStream(fos);
} else {
    // 파일에 데이터가 있으면 커스텀 클래스 (헤더 없음)
    oos = new AppendableObjectOutputStream(fos);
}
oos.writeObject(newMember);
oos.close();
```
### 읽는 방법
```java
List<Member> members = new ArrayList<>();
try (ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream("members.dat"))) {
    while (true) {
        try {
            members.add((Member) ois.readObject());
            // 더 이상 읽을 게 없으면 null 반환x, EOFException 던진다.
        } catch (EOFException e) {
            break;
        }
    }
}
```
- 스트림 헤더: 이 파일이 직렬화된 데이터라고 알려주는 표지판
```
[AC ED 00 05] [객체1 데이터] [객체2 데이터] [객체3 데이터]
     ↑
   헤더 (4바이트)
```
- AC ED 00 05: 직렬화 매직 넘버와 버전 정보
- ObjectInputStream이 파일을 읽을 때 맨 처음 이걸 확인함
- 이게 Java 직렬화 파일이 맞구나를 체크하는 용도
- ObjectInputStream은 파일 맨 앞에서만 헤더를 기대
- 내부 캐시: 이미 저장한 객체를 기억하는 메모장
```java
Member member = new Member("홍길동", 20);
oos.writeObject(member);
oos.writeObject(member);
oos.writeObject(member);
// [헤더] [member 전체 데이터] [참조1] [참조1]
// 용량 절약: 같은 데이터를 반복해서 안 씀
// 성능 향상: 이미 저장한 건 건너뜀
// 캐시는 객체의 메모리 주소를 보고 판단
```
- reset()이 하는 일
```java
Member m1 = new Member("홍길동", 20);
oos.writeObject(m1); // 완전히 저장
// 캐시: {m1 → 참조번호1}
oos.reset(); // 캐시를 비움
// 캐시: {} (비어있음)
oos.writeObject(m1); // 다시 완전히 저장
```
- 커스텀 클래스에서 왜 reset()을 쓸까
- 헤더를 안 쓰기 위해서 reset()을 쓰는 건 아니다.
- writeStreamHeader()를 빈 메서드로 오버라이드 → 헤더를 안 씀
- 그냥 비워두면 스트림이 초기화 안 될 수 있어서 reset()으로 스트림 내부 정리
- 헤더를 안 쓰는 건 writeStreamHeader() 오버라이드 덕분이다.
### AC ED 00 05의 의미
- 대부분의 Java 직렬화 파일은 이 값으로 시작한다.

| 바이트 | 16진수 | 의미 |
|--------|-------|------|
| 1-2 | AC ED | 매직 넘버 (Magic Number) - Java 직렬화 파일 |
| 3-4 | 00 05 | 버전 넘버 (Version) - 직렬화 프로토콜 버전 5 |

- 매직 넘버란 파일 형식을 식별하는 고유한 값
- PNG 이미지: `89 50 4E 47`
- ZIP 파일: `50 4B 03 04`
- Java 직렬화: `AC ED`
### ObjectInputStream 객체 저장
- 서로 다른 클래스 객체 저장 가능하다. 하지만 잘 안 쓴다.
- 읽은 값 캐스팅할 때 저장한 순서를 기억하거나 instanceof로 체크해야 한다.
- 같은 타입만 여러 개 저장하거나
- 같은 타입만 리스트로 묶어서 리스트 하나만 저장하거나
- 타입별 리스트를 담은 클래스를 하나만 저장하거나 한다.
### transient 키워드
- 해당 필드는 직렬화하지 않는다.
- 보안 데이터, 임시 데이터, 직렬화 안 되는 객체에 쓴다.
```java
class Member implements Serializable {
    String name;
    int age;
    transient String password; // 직렬화 제외
}
Member m = new Member("홍길동", 20, "secret123");
// 저장하면: name, age만 저장됨
// password는 저장 안 됨
```
- 객체를 직렬화한다 = 객체를 바이트로 변환한다.
- 초기에는 객체를 직렬화해서 스트림으로 많이 전송했다.
- 현대에는 잘 안 쓰이고 다른 방법들을 쓴다.