package chat.client;

import java.io.IOException;

public class ClientMain2 {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", PORT);
        client.start();
    }
}
/*
20:07:34.181 [     main] 클라이언트 시작
20:07:34.196 [ Thread-0] ReadHandler 실행
20:07:34.196 [ Thread-1] WriteHandler 실행
이름을 입력하세요: rla
rla님이 입장함
sk님이 입장함
dkssud
[rla] dkssud
[sk] gkdl
[sk] exit
sk님이 퇴장함
/exit
20:08:22.525 [ Thread-1] 클라이언트 종료
20:08:22.525 [ Thread-1] WriteHandler 종료
20:08:22.525 [ Thread-1] ReadHandler 종료
20:08:22.525 [ Thread-0] 클라이언트 종료
java.io.EOFException
	at java.base/java.io.DataInputStream.readFully(DataInputStream.java:210)
	at java.base/java.io.DataInputStream.readUnsignedShort(DataInputStream.java:341)
	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:575)
	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:550)
	at chat.client.ReadHandler.run(ReadHandler.java:21)
	at java.base/java.lang.Thread.run(Thread.java:1583)
 */