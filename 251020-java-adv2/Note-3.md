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