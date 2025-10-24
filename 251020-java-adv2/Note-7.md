## Suppressed Exception
- 한 예외가 이미 발생했는데, 그 뒤에 또 다른 예외가 생겼을 때, 나중의 예외 때문에 첫 번째 예외가 사라지지 않도록 저장해두는 메커니즘
- 자바 7부터 추가된 기능
- try-with-resources 문법에서 자동으로 사용
### 설명
- 자바는 기본적으로 마지막 예외만 던진다는 규칙이 있다.
- 하지만 첫 번째 예외가 더 중요한 원인일 수도 있다.
- 나중Exception을 던지되, 처음Exception를 suppressed exception으로 저장해 둔다.
### 확인하는 방법
```java
// try-with-resources에서는 자동 처리된다.
try {
    logic();
} catch (Exception e) {
    e.printStackTrace(); // 메인 예외 출력
    for (Throwable t : e.getSuppressed()) {
        System.out.println("Suppressed: " + t);
    }
}
//CloseException: r2 closeEx
//    ...
//Suppressed: CallException: r2 callEx
```
- finally 블록에서 예외 발생 시 나머지 실행 안 됨
```java
// 이렇게 방지한다.
finally {
    try { one.close(); } catch (Exception e) { /* 로그 */ }
    try { two.close(); } catch (Exception e) { /* 로그 */ }
    try { three.close(); } catch (Exception e) { /* 로그 */ }
}
```
- 251020-java-adv2/src/autoclose/Main3.java
```java
/*
r1 call
r2 callEx
r1 close
r2 closeEx
autoclose.CloseException: r2 ex // autoclose(패키지 이름)에 정의된 CloseException 클래스
// 패키지 포함된 클래스 이름을 클래스의 전체 이름(fully qualified name)이라고 한다.
// r2 ex: 예외 메시지 (super(msg)에 전달된 값)
r2 callEx
autoclose.CallException: r2 ex
callEx 예외 처리 // finally에서 새로운 예외가 던져지지 않는 한 try에서 발생했던 예외가 main으로 전파된다.
*/
```
- 251020-java-adv2/src/network4 // SocketCloseUtil
- 251020-java-adv2/src/network5 // try-with-resources
- 251020-java-adv2/src/network6 // Shutdown Hook
- 자바는 프로세스가 종료될 때 자원 정리나 로그 기록과 같은 종료 작업을 마무리할 수 있는 셧다운 훅이라는 기능을 지원한다.
- 1. 프로세스 종료 - 정상 종료
- 모든 non-demon 스레드의 실행 완료로 자바 프로세스 정상 종료
- 사용자가 Ctrl + C를 눌러서 프로그램을 중단
- kill 명령 전달 (kill -9 제외)
- IntelliJ의 stop 버튼
- 2. 프로세스 종료 - 강제 종료
- 운영체제에서 프로세스를 유지할 수 없다고 판단할 때 사용
- 리눅스/유닉스의 kill -9나 Windows의 taskkill /F
- 정상 종료: 셧다운 훅이 작동해서 프로세스 종료 전에 필요한 처리를 할 수 있다.
- 강제 종료: 셧다운 훅이 작동하지 않는다.