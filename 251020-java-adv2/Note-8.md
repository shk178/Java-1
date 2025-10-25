## 정상 종료 (Graceful Shutdown) - 4-Way Handshake
### 1. 클라이언트가 "exit" 입력 후 정상 종료
```java
// 클라이언트 코드
output.writeUTF("exit");  // 1. 먼저 데이터 전송
// try-with-resources 블록 종료
socket.close();  // 2. 소켓 닫기
```
패킷 교환 과정:
```
클라이언트                              서버
─────────────────────────────────────────────────
1. PSH, ACK [exit 데이터]
   Seq=1000, Ack=2000
   Data: "exit"
                    ────────────>
                                          2. 서버가 데이터 수신
                                             input.readUTF() 반환
                    <────────────
                                          3. ACK
                                             Seq=2000, Ack=1005
                                             (데이터 받았음을 확인)
4. socket.close() 호출
   OS가 FIN 패킷 전송
   FIN, ACK
   Seq=1005, Ack=2000
                    ────────────>
                                          5. 서버가 FIN 수신
                                             "클라이언트가 더 이상 
                                              보낼 데이터 없다"
                    <────────────
                                          6. ACK
                                             Seq=2000, Ack=1006
                                             (FIN 받았음을 확인)
[CLOSE_WAIT 상태]                          [FIN_WAIT_2 상태]
                                          7. 서버도 session.close()
                    <────────────
                                          8. FIN, ACK
                                             Seq=2000, Ack=1006
9. 클라이언트 FIN 수신
   ACK
   Seq=1006, Ack=2001
                    ────────────>
                                          10. 서버가 ACK 수신
[TIME_WAIT 상태]                           [CLOSED]
2MSL 대기 (보통 1-4분)
      ↓
[CLOSED]
```
## 각 패킷의 구조
### FIN 패킷 (정상 종료)
```
┌─────────────────────────────────────┐
│ IP Header                           │
│ ├─ Source IP: 192.168.0.10         │
│ └─ Dest IP: 192.168.0.20           │
├─────────────────────────────────────┤
│ TCP Header                          │
│ ├─ Source Port: 54321              │
│ ├─ Dest Port: 12345                │
│ ├─ Sequence Number: 1005           │
│ ├─ ACK Number: 2000                │
│ ├─ Flags: FIN, ACK ← 핵심          │
│ │   - FIN = 1 (종료 요청)          │
│ │   - ACK = 1                      │
│ ├─ Window Size: 65535              │
│ └─ Checksum                        │
├─────────────────────────────────────┤
│ Data: (없음)                        │
└─────────────────────────────────────┘
```
### FIN의 의미:
```
"나는 더 이상 보낼 데이터가 없습니다.
 하지만 당신이 보내는 건 계속 받을 수 있습니다."
이것을 Half-Close라고 합니다.
```
## 강제 종료 (Abortive Shutdown) - RST
### 2. 클라이언트가 Ctrl+C로 강제 종료
```bash
# 클라이언트 실행 중
java Client
전송 문자: Hello
^C  ← Ctrl+C 입력
```
패킷 교환 과정:
```
클라이언트                              서버
─────────────────────────────────────────────────
[정상 통신 중]
1. Ctrl+C
   JVM이 즉시 종료 신호 받음
   OS가 프로세스 강제 종료
2. OS가 열린 소켓들 정리
   "정상 종료 시간 없음"
   RST (Reset)
   Seq=1005, Ack=2000
                    ────────────>
                                          3. 서버가 RST 수신
                                             "갑자기 끊김"
                                          4. input.readUTF() 실행 중이었다면
                                             IOException 발생
                                             "Connection reset by peer"
[CLOSED]                                  5. session.close() 실행
즉시 종료                                  소켓 정리
                                          [CLOSED]
```
### RST 패킷 (강제 종료)
```
┌─────────────────────────────────────┐
│ TCP Header                          │
│ ├─ Source Port: 54321              │
│ ├─ Dest Port: 12345                │
│ ├─ Sequence Number: 1005           │
│ ├─ ACK Number: 0 ← ACK 없음        │
│ ├─ Flags: RST ← 핵심                │
│ │   - RST = 1 (즉시 종료)          │
│ │   - ACK = 0                      │
│ │   - FIN = 0                      │
│ ├─ Window Size: 0                  │
│ └─ Checksum                        │
├─────────────────────────────────────┤
│ Data: (없음)                        │
└─────────────────────────────────────┘
```
### RST의 의미:
```
"연결을 즉시 끊습니다
 확인 응답도 필요 없어요.
 버퍼에 있는 데이터도 모두 버립니다"
```

| 구분 | 정상 종료 (FIN) | 강제 종료 (RST) |
|------|---------|----------|
| 패킷 수 | 4개 (4-way handshake) | 1개 |
| ACK 필요 | 각 단계마다 필요 | 불필요 |
| 버퍼 처리 | 버퍼의 데이터 모두 전송 | 버퍼 데이터 즉시 버림 |
| Half-Close | 가능 (한쪽만 종료) | 불가능 (양쪽 즉시 종료) |
| TIME_WAIT | 있음 (1-4분) | 없음 (즉시 종료) |
| 상대방 통지 | 정상적으로 알림 | 갑작스런 단절 |
| 에러 발생 | 정상 종료 | IOException |

### 시나리오 1: 정상 종료
```java
// 클라이언트
try (Socket socket = new Socket("localhost", 12345)) {
    // ... 통신 ...
    output.writeUTF("exit");
} // ← try-with-resources가 socket.close() 호출
  // → FIN 패킷 전송
```
결과:
```
서버: "클라이언트가 정상적으로 연결을 끊었습니다"
로그: "연결 종료: /192.168.0.10:54321"
finally 블록 정상 실행
```
### 시나리오 2: 프로세스 강제 종료 (Ctrl+C)
```bash
java Client
전송 문자: Hello
^C  ← 갑자기 종료
```
결과:
```
서버: IOException 발생
로그: "세션 에러: Connection reset by peer"
input.readUTF()가 예외 던짐
finally 블록은 실행됨 (예외 처리)
```
### 시나리오 3: 네트워크 케이블 뽑음
```
클라이언트의 네트워크 케이블을 물리적으로 뽑음
```
결과:
```
RST도 FIN도 전송 안 됨 (네트워크 끊김)
서버는 계속 readUTF()에서 대기 중...
TCP Keepalive 타임아웃까지 대기 (기본 2시간 )
또는 소켓 타임아웃 설정 시 그때 에러:
   "SocketTimeoutException: Read timed out"
```
이를 방지하려면:
```java
socket.setSoTimeout(30000); // 30초 타임아웃 설정
try {
    String data = input.readUTF();
} catch (SocketTimeoutException e) {
    // 30초간 데이터 안 오면 예외 발생
    close();
}
```
### 시나리오 4: SO_LINGER 옵션
```java
// 소켓 닫을 때 대기 시간 설정
socket.setSoLinger(true, 5); // 5초 대기
socket.close();
```
동작:
```
socket.close() 호출
      ↓
"5초간 버퍼의 데이터를 전송 시도"
      ↓
5초 내에 전송 완료 → FIN 전송 (정상 종료)
  x → RST 전송 (강제 종료, 나머지 데이터 버림)
```
## TCP 상태 다이어그램
### 정상 종료 시 상태 변화
```
클라이언트 측:
ESTABLISHED (연결 중)
      ↓
close() 호출, FIN 전송
      ↓
FIN_WAIT_1 (FIN 전송, ACK 대기)
      ↓
상대방 ACK 수신
      ↓
FIN_WAIT_2 (상대방 FIN 대기)
      ↓
상대방 FIN 수신, ACK 전송
      ↓
TIME_WAIT (2MSL 대기, 보통 1-4분)
      ↓
CLOSED
서버 측:
ESTABLISHED (연결 중)
      ↓
FIN 수신, ACK 전송
      ↓
CLOSE_WAIT (애플리케이션이 close() 할 때까지 대기)
      ↓
close() 호출, FIN 전송
      ↓
LAST_ACK (ACK 대기)
      ↓
ACK 수신
      ↓
CLOSED
```
### 강제 종료 시 상태 변화
```
클라이언트/서버 양쪽:
ESTABLISHED (연결 중)
      ↓
RST 전송 또는 수신
      ↓
CLOSED (즉시)
중간 상태 없음
```
### Wireshark로 본 정상 종료
```
No. Time    Source          Dest            Protocol Info
1   0.000   192.168.0.10    192.168.0.20    TCP      [PSH,ACK] Len=9 [exit]
2   0.001   192.168.0.20    192.168.0.10    TCP      [ACK]
3   0.100   192.168.0.10    192.168.0.20    TCP      [FIN,ACK] Seq=1009
4   0.101   192.168.0.20    192.168.0.10    TCP      [ACK] Ack=1010
5   0.150   192.168.0.20    192.168.0.10    TCP      [FIN,ACK] Seq=2000
6   0.151   192.168.0.10    192.168.0.20    TCP      [ACK] Ack=2001
총 6개 패킷 (데이터 전송 2개 + 4-way handshake)
```
### Wireshark로 본 강제 종료
```
No. Time    Source          Dest            Protocol Info
1   0.000   192.168.0.10    192.168.0.20    TCP      [PSH,ACK] Len=5 [Hello]
2   0.001   192.168.0.20    192.168.0.10    TCP      [ACK]
3   0.100   192.168.0.10    192.168.0.20    TCP      [RST] Seq=1005
총 3개 패킷 (데이터 전송 2개 + RST 1개)
```
### TIME_WAIT 없이 즉시 종료하면
```
클라이언트                              서버
─────────────────────────────────────────────────
FIN, ACK (Seq=1005)
                    ────────────>
                    <────────────
                                   ACK (Ack=1006)
                    <───── 네트워크 지연으로
                           FIN 패킷 늦게 도착
클라이언트 즉시 CLOSED
새로운 연결 시작
같은 포트 54321 재사용
                    <────────────
                                   (늦게 도착한) FIN
"이건 이전 연결의 패킷인데
 새 연결로 잘못 전달됨"
```
### TIME_WAIT로 해결
```
클라이언트가 FIN 보낸 후
2MSL (Maximum Segment Lifetime) 동안 대기
(보통 30초 ~ 4분)
이 시간 동안:
1. 늦게 도착하는 패킷들을 모두 처리
2. 같은 포트를 다른 연결이 재사용 못 하게 방지
3. 네트워크에서 중복 패킷 제거
```
### 1. 정상 종료 보장하기
```java
// Good
try (Socket socket = new Socket(...)) {
    // 통신
} // 자동으로 close() → FIN 전송
// Bad
Socket socket = new Socket(...);
// close() 안 함 → 리소스 누수
```
### 2. 강제 종료 감지하기
```java
try {
    String data = input.readUTF();
} catch (EOFException e) {
    // 정상 종료 (FIN 받음)
    log("클라이언트가 정상적으로 종료");
} catch (SocketException e) {
    // 강제 종료 (RST 받음)
    log("클라이언트가 강제로 종료: " + e.getMessage());
    // "Connection reset"
}
```
### 3. 타임아웃 설정
```java
socket.setSoTimeout(30000); // 30초
// readUTF()가 30초 넘게 블로킹되면 
// SocketTimeoutException 발생
```
## Seq와 Ack의 기본 개념
```
Seq (Sequence Number): 
"내가 보내는 데이터의 시작 번호"
Ack (Acknowledgment Number):
"다음에 받고 싶은 데이터의 번호"
= "여기까지 잘 받았어요"
```
### TCP는 데이터를 바이트 스트림으로 봅니다
```
클라이언트가 보낼 데이터:
┌───┬───┬───┬───┬───┬───┬───┬───┬───┐
│ H │ e │ l │ l │ o │ W │ o │ r │ d │
└───┴───┴───┴───┴───┴───┴───┴───┴───┘
1000 1001 1002 1003 1004 1005 1006 1007 1008
각 바이트마다 번호가 매겨져 있음
Seq=1000 의미: "1000번 바이트부터 시작하는 데이터를 보냅니다"
```
### 예시: "Hello" (5바이트) 전송
```
초기 상태:
클라이언트: 다음 보낼 바이트 = 1000
서버: 다음 보낼 바이트 = 2000
```
### 1단계: 클라이언트 → 서버 (데이터 전송)
```
클라이언트                              서버
─────────────────────────────────────────────────
Seq=1000, Ack=2000
Data: "Hello" (5바이트)
                    ────────────>
의미:
- Seq=1000: "1000번 바이트부터 데이터를 보냅니다"
- Ack=2000: "서버야, 너의 2000번 바이트를 기다리고 있어"
- Data: 1000, 1001, 1002, 1003, 1004 (총 5바이트)
서버가 받은 후:
"클라이언트가 1000~1004를 보냈구나"
"다음은 1005를 기다리면 되겠네"
```
### 2단계: 서버 → 클라이언트 (응답)
```
                    <────────────
Seq=2000, Ack=1005
의미:
- Seq=2000: "내(서버)가 2000번 바이트부터 보낼게"
- Ack=1005: "클라이언트야, 1005번 바이트를 보내줘"
            = "1004번까지 잘 받았어"
클라이언트가 받은 후:
"아, 서버가 내 데이터를 1004번까지 받았구나"
"다음은 1005부터 보내야지"
```
### 3단계: 클라이언트 → 서버 (추가 데이터)
```
Seq=1005, Ack=2000
Data: "World" (5바이트)
                    ────────────>
의미:
- Seq=1005: "1005번 바이트부터 시작해" (이전이 1004까지였으니)
- Ack=2000: "서버 데이터는 아직 2000번을 기다려"
- Data: 1005, 1006, 1007, 1008, 1009 (총 5바이트)
```
```
시간 흐름 →
클라이언트 Seq 위치:        서버 Seq 위치:
1000                        2000
  ↓ "Hello" 5바이트 전송      ↓
1005                        2000
  ↓ ACK 수신                  ↓ "Hello" 수신, ACK 전송
1005                        2000
  ↓ "World" 5바이트 전송      ↓
1010                        2000
  ↓                          ↓ "World" 수신, ACK 전송
1010                        2000
```
```
클라이언트                              서버
─────────────────────────────────────────────────
초기 상태:
Client Seq: 1000                    Server Seq: 2000
Client Ack: 2000                    Server Ack: 1000
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
클라이언트: "Hello" 전송
[TCP Header]
Seq=1000 ← "내 데이터는 1000번부터"
Ack=2000 ← "너의 2000번을 기다려"
[Data: Hello] (5바이트)
                    ────────────>
                                    "1000~1004 받음"
                                    "다음은 1005"
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
서버: ACK 응답
                    <────────────
                                    [TCP Header]
                                    Seq=2000 ← "내 차례는 2000번부터"
                                    Ack=1005 ← "1005 주세요"
                                              (= "1004까지 받았어요")
"오케이, 1004까지 
 잘 받았구나"
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
서버: "Hello World" 응답 (11바이트)
                    <────────────
                                    [TCP Header]
                                    Seq=2000 ← "2000번부터 시작"
                                    Ack=1005 ← "여전히 1005 기다려"
                                    [Data: Hello World] (11바이트)
"2000~2010 받음"
"다음은 2011"
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
클라이언트: ACK 응답
[TCP Header]
Seq=1005 ← "내 차례는 1005부터"
Ack=2011 ← "2011 주세요" (= "2010까지 받음")
                    ────────────>
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
현재 상태:
Client Seq: 1005                    Server Seq: 2011
Client Ack: 2011                    Server Ack: 1005
```
### 중간에 패킷이 사라지면
```
클라이언트                              서버
─────────────────────────────────────────────────
1️⃣ Seq=1000, Data: "Hello" (5바이트)
                    ────────────>
                                    ✅ 1000~1004 받음
2️⃣ Ack=1005
                    <────────────
✅ ACK 받음
3️⃣ Seq=1005, Data: "World" (5바이트)
                    ────❌────>  패킷 손실
4️⃣ Seq=1010, Data: " " (1바이트)
                    ────────────>
                                    ❌ 1010을 받았는데
                                       1005~1009가 없음
5️⃣ 서버: "1005가 없네"
                    <────────────
                                    Ack=1005 (중복 ACK)
                                    "여전히 1005를 기다려"
6️⃣ 클라이언트: "아, 1005 재전송해야겠다"
   Seq=1005, Data: "World" (재전송)
                    ────────────>
                                    ✅ 이제 1005~1009 받음
                                    ✅ 1010도 이미 받았음
7️⃣ Ack=1011
                    <────────────
                                    "1010까지 다 받았어요"
```
## 초기 Seq는 3-Way Handshake에서 결정
```
클라이언트                              서버
─────────────────────────────────────────────────
1️⃣ SYN
   Seq=1000 ← 랜덤하게 선택 (ISN: Initial Sequence Number)
   Ack=0    ← 아직 없음
                    ────────────>
                                    "클라이언트는 1000부터 시작하네"
2️⃣ SYN, ACK
                    <────────────
                                    Seq=2000 ← 서버도 랜덤 선택
                                    Ack=1001 ← "1000 받았으니 1001 주세요"
                                               (SYN도 1바이트로 취급)
3️⃣ ACK
   Seq=1001 ← "1001부터 시작"
   Ack=2001 ← "2000 받았으니 2001 주세요"
                    ────────────>
연결 완료
클라이언트: 1001부터 시작
서버: 2001부터 시작
```
왜 랜덤
```
보안상의 이유:
- 예측 가능하면 공격자가 위조 패킷 만들기 쉬움
- 랜덤하면 정상 연결인지 확인 가능
실제로는:
- 0~4,294,967,295 (32비트) 범위에서 선택
- 시간 기반 알고리즘 사용
```
### Java 코드와 Seq/Ack 관계
```java
// 클라이언트
output.writeUTF("Hello");  // 5바이트 전송
// → TCP: Seq=1000, Data="Hello"
// → 내부적으로 Seq가 1000→1005로 증가
String response = input.readUTF();  // 대기
// → TCP: 서버로부터 Seq=2000, Ack=1005 받음
// → "Hello World" 수신 (11바이트)
// → 내부적으로 ACK 전송: Seq=1005, Ack=2011
```
OS가 자동으로 처리:
```
애플리케이션 (Java):
"Hello" 문자열만 신경 씀
TCP 스택 (OS Kernel):
├─ Seq 번호 관리
├─ Ack 번호 계산
├─ 재전송 타이머 설정
├─ 패킷 순서 재정렬
└─ 중복 ACK 감지
네트워크 카드:
전기 신호로 변환
```
### 실제 tcpdump 출력 예시
```bash
tcpdump -i eth0 port 12345 -nn
출력:
14:30:00.000000 IP 192.168.0.10.54321 > 192.168.0.20.12345: 
    Flags [P.], seq 1000:1005, ack 2000, win 65535, length 5
    # seq 1000:1005 = 1000번부터 1004번까지 (5바이트)
    # ack 2000 = 다음에 2000번 기대
14:30:00.001000 IP 192.168.0.20.12345 > 192.168.0.10.54321: 
    Flags [.], seq 2000, ack 1005, win 65535, length 0
    # seq 2000 = 서버는 2000번부터
    # ack 1005 = 1005번 기대 (1004까지 받음)
14:30:00.100000 IP 192.168.0.20.12345 > 192.168.0.10.54321: 
    Flags [P.], seq 2000:2011, ack 1005, win 65535, length 11
    # seq 2000:2011 = 2000~2010 (11바이트)
    # "Hello World" 응답
```
### Seq (Sequence Number)
```
내가 보내는 데이터의 첫 바이트 번호
데이터 크기만큼 자동 증가
상대방이 순서를 맞춰 재조립하는 데 사용
예:
Seq=1000, Data 5바이트 전송
→ 다음 Seq=1005
```
### Ack (Acknowledgment Number)
```
다음에 받고 싶은 바이트 번호
"여기까지 받았어요"의 의미
누적 확인 (cumulative ACK)
예:
Ack=1005
→ "1004번까지 잘 받았고, 1005번 주세요"
상대방의 Seq + 데이터크기 = 내 Ack
예:
상대방: Seq=1000, Data 5바이트
나: Ack=1000+5=1005
```
## FIN 패킷의 Seq/Ack
SYN, FIN도 1바이트
```
클라이언트                              서버
─────────────────────────────────────────────────
현재 상태: Seq=1005, Ack=2000
FIN, ACK
Seq=1005 ← "데이터는 1005에서 끝"
Ack=2000 ← "서버 2000 기다려"
                    ────────────>
                                    "FIN도 1바이트로 취급"
                    <────────────
                                    ACK
                                    Seq=2000
                                    Ack=1006 ← "FIN 받았어요"
                                               (1005 + 1 = 1006)
```
- 251020-java-adv2/src/exception/NormalCloseClient.java
### 1. readByInputStream() - `input.read()`
```java
int read = input.read(); // ← 여기서 블로킹
if (read == -1) {        // ← FIN 받으면 -1 반환
    input.close();       // 스트림만 닫음 (네트워크 패킷 없음)
    socket.close();      // ← 여기서 FIN+ACK 전송 
}
```
패킷 흐름:
```
서버                                     클라이언트
─────────────────────────────────────────────────
서버가 socket.close() 호출
      ↓
FIN, ACK
Seq=2000, Ack=1000
                        ────────────>
                                         OS 커널이 FIN 수신
                                         소켓 버퍼에 "EOF" 표시
                                         ↓
                                         input.read() 반환
                                         return -1 ← "EOF 도달"
                                         ↓
                                         if (read == -1) 실행
                                         ↓
                                         input.close()
                                         (내부 버퍼만 정리, 패킷 없음)
                                         ↓
                                         socket.close() 호출
                                         ↓
ACK                                      JVM → OS 시스템 콜
Seq=2000, Ack=1001
                        <────────────
                                         FIN, ACK 전송
                                         Seq=1000, Ack=2001
```
### 2. readByBufferedReader() - `readLine()`
```java
String line = reader.readLine(); // ← 여기서 블로킹
if (line == null) {              // ← FIN 받으면 null 반환
    reader.close();              // BufferedReader 닫음 (패킷 없음)
    socket.close();              // ← 여기서 FIN+ACK 전송 
}
```
내부 동작:
```
reader.readLine() 호출
      ↓
BufferedReader가 내부 버퍼 확인
      ↓
버퍼 비어있음 → 하위 스트림에서 읽기
      ↓
InputStreamReader.read() 호출
      ↓
InputStream.read() 호출 (네이티브 메서드)
      ↓
JVM → OS 시스템 콜: read()
      ↓
OS 커널: "소켓 버퍼에 EOF 표시되어 있음"
      ↓
OS → JVM: -1 반환
      ↓
InputStreamReader: -1 받음
      ↓
BufferedReader: "스트림 끝" 인식
      ↓
return null ← "EOF 도달"
```
패킷 흐름:
```
서버                                     클라이언트
─────────────────────────────────────────────────
FIN, ACK (Seq=2000)
                        ────────────>
                                         OS가 소켓 버퍼에 EOF 표시
                                         ↓
                                         readLine() → null
                                         ↓
                                         reader.close()
                                         (계층별 버퍼 정리, 패킷 없음)
                                         ↓
                                         socket.close()
                                         ↓
                        <────────────    ACK (Ack=2001)
                        <────────────    FIN, ACK (Seq=1000)
```
### 3. readByDataInputStream() - `readUTF()`
```java
try {
    dis.readUTF(); // ← 여기서 블로킹 & 예외 발생 
} catch (EOFException e) {
    e.printStackTrace();
} finally {
    dis.close();    // DataInputStream 닫음 (패킷 없음)
    socket.close(); // ← 여기서 FIN+ACK 전송 
}
```
왜 EOFException이 발생하나
```
readUTF()의 내부 동작:
1단계: 길이 읽기 (2바이트)
       int len = readUnsignedShort();
       ↓
       첫 번째 바이트 읽기 → -1 (EOF)
       ↓
       "2바이트를 읽어야 하는데 EOF"
       ↓
       throw new EOFException(); ← 예외 발생 
readLine()과의 차이:
- readLine(): EOF를 만나면 null 반환 (정상)
- readUTF(): EOF를 만나면 예외 발생 (비정상)
  왜? UTF 형식은 반드시 길이 헤더가 있어야 함 
```
스택 트레이스 분석:
```
java.io.EOFException
  at DataInputStream.readFully()        ← 여기서 실제 read() 호출
  at DataInputStream.readUnsignedShort() ← 2바이트 길이 읽기 시도
  at DataInputStream.readUTF()          ← readUnsignedShort() 호출
  at NormalCloseClient.readByDataInputStream()
```
패킷 흐름:
```
서버                                     클라이언트
─────────────────────────────────────────────────
FIN, ACK (Seq=2000)
                        ────────────>
                                         OS가 EOF 표시
                                         ↓
                                         dis.readUTF() 실행
                                         ├─ readUnsignedShort() 호출
                                         ├─ read() → -1
                                         └─ throw EOFException
                                         ↓
                                         catch 블록 실행
                                         e.printStackTrace()
                                         ↓
                                         finally 블록 실행
                                         dis.close()
                                         socket.close()
                                         ↓
                        <────────────    ACK (Ack=2001)
                        <────────────    FIN, ACK (Seq=1000)
```
### 계층별 close() 분석
```
socket.close()           ← OS에 "연결 종료" 요청 → FIN 패킷 전송
   ↓ (내부적으로 호출)
input.close()            ← 소켓의 입력 스트림만 닫음 (패킷 없음)
   ↓
output.close()           ← 소켓의 출력 스트림만 닫음 (패킷 없음)
```
각각의 close():
```java
// 1. 스트림만 닫기 (패킷 전송 안 함)
input.close();
// - 내부 버퍼만 정리
// - 파일 디스크립터는 그대로
// - 네트워크 패킷 전송 없음
// 2. BufferedReader 닫기 (패킷 전송 안 함)
reader.close();
// - BufferedReader 버퍼 정리
// - 내부적으로 InputStreamReader.close() 호출
// - InputStreamReader가 InputStream.close() 호출
// - 하지만 여전히 패킷 전송 없음
// 3. 소켓 닫기 (패킷 전송 )
socket.close();
// - JVM → OS 시스템 콜: close(fd)
// - OS가 TCP FIN 패킷 전송
// - 파일 디스크립터 제거
```
### OS 레벨에서 무슨 일이
```
서버가 socket.close() 호출
      ↓
┌─────────────────────────────┐
│ 서버 OS (Kernel)            │
├─────────────────────────────┤
│ TCP 스택:                   │
│ - FIN 패킷 생성             │
│ - Seq=2000, Ack=1000        │
│ - 네트워크 카드로 전송       │
└─────────────────────────────┘
      ↓
네트워크 전송
      ↓
┌─────────────────────────────┐
│ 클라이언트 OS (Kernel)       │
├─────────────────────────────┤
│ TCP 스택:                   │
│ 1. FIN 패킷 수신            │
│ 2. 소켓 상태: CLOSE_WAIT    │
│ 3. 소켓 버퍼에 EOF 마크     │
│ 4. ACK 자동 전송 (Ack=2001) │
└─────────────────────────────┘
      ↓
클라이언트 Java 프로그램
input.read() 블로킹 중...
      ↓
OS가 스레드 깨움
      ↓
return -1 ← "EOF"
      ↓
애플리케이션이 socket.close() 호출
      ↓
┌─────────────────────────────┐
│ 클라이언트 OS (Kernel)       │
├─────────────────────────────┤
│ TCP 스택:                   │
│ - FIN 패킷 생성             │
│ - Seq=1000, Ack=2001        │
│ - 네트워크 카드로 전송       │
└─────────────────────────────┘
```
### 권장 방법:
```java
// Good
try (Socket socket = new Socket(...);
     DataInputStream dis = new DataInputStream(socket.getInputStream())) {
    
    dis.readUTF();
    
} // try-with-resources가 자동으로:
  // 1. dis.close() → 스트림 버퍼 정리
  // 2. socket.close() → FIN 패킷 전송
```
### 수동 종료:
```java
// Good - 명시적 순서
DataInputStream dis = new DataInputStream(socket.getInputStream());
try {
    dis.readUTF();
} finally {
    dis.close();     // 1. 스트림 먼저 정리 (버퍼 flush)
    socket.close();  // 2. 소켓 닫기 (FIN 전송)
}
// 순서 바뀌면
socket.close();  // FIN 전송 → 연결 끊김
dis.close();     // 이미 연결 끊긴 상태에서 버퍼 정리
                 // 데이터 손실 가능 
```
```
서버                                     클라이언트
─────────────────────────────────────────────────
서버: socket.close()
      ↓
1️⃣ FIN, ACK (Seq=2000, Ack=1000)
                        ────────────>
                                         OS가 자동으로 ACK 전송
                        <────────────
                                         2️⃣ ACK (Seq=1000, Ack=2001)
                                         input.read() → -1
                                         애플리케이션: socket.close()
                                         ↓
                        <────────────
                                         3️⃣ FIN, ACK (Seq=1000, Ack=2001)
OS가 자동으로 ACK 전송
      ↓
4️⃣ ACK (Seq=2001, Ack=1001)
                        ────────────>
[CLOSED]                                 [TIME_WAIT]
                                         (2MSL 대기)
                                         ↓
                                         [CLOSED]
```
- 1. `read()`, `readLine()`, `readUTF()` 등이 FIN을 감지합니다
- 실제로는 OS 커널이 FIN 받고 소켓 버퍼에 EOF 표시
- Java의 읽기 메서드가 이를 감지하여 반환
- 2. `socket.close()`가 FIN+ACK를 전송합니다
- OS 시스템 콜을 통해 TCP 스택에 종료 요청
- TCP 스택이 FIN 패킷 생성 및 전송
- 3. `input.close()`, `reader.close()` 등은 패킷을 보내지 않습니다
- 단지 내부 버퍼 정리
- 메모리 해제
- 리소스 정리
- 251020-java-adv2/src/exception/ResetCloseClient.java
## RST가 발생하는 이유
### 흐름 요약:
- 서버는 클라이언트의 연결을 수락한 뒤 바로 `socket.close()`를 호출해서 TCP 연결을 종료 (FIN 전송)
- 클라이언트는 서버가 FIN을 보낸 뒤에도 `output.write(1)`을 시도함 → 이미 닫힌 연결에 데이터를 쓰려 함
- 이때 OS는 RST(Reset) 패킷을 서버 측에서 클라이언트로 전송함 → TCP 연결을 강제로 종료
### 왜 RST가 발생하냐면:
- TCP에서는 연결 종료 시 FIN→ACK 순서로 정상 종료를 처리함
- 그런데 클라이언트가 FIN을 받은 뒤에도 데이터를 보내면
- 서버는 이를 비정상적인 동작으로 간주하고 RST 패킷을 보내 연결을 강제 종료함
- 이건 TCP의 보호 메커니즘 - "이미 닫은 연결인데 왜 데이터를 보내"라는 반응

| 예외 종류 | 발생 조건 |
|-------|-----------|
| `Connection reset` | 읽기(read) 시, 상대방이 RST를 보낸 경우 |
| `Broken pipe` | 쓰기(write) 시, 상대방이 연결을 닫았거나 RST를 보낸 경우 |

- `output.write(1)` → 서버가 이미 닫았기 때문에 Broken pipe가 발생할 수 있음 (OS마다 다름)
- `input.read()` → 서버가 RST를 보냈기 때문에 Connection reset이 발생함
- 연결 타임아웃은 초기 TCP 연결 수립에 걸리는 최대 시간
- 소켓 타임아웃은 연결 후 데이터 읽기를 기다리는 최대 시간