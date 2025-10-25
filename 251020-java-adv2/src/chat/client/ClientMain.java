package chat.client;

import java.io.IOException;

public class ClientMain {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", PORT);
        client.start();
    }
}
/*
19:52:11.258 [     main] 클라이언트 시작
19:52:11.294 [ Thread-0] ReadHandler 실행
19:52:11.294 [ Thread-1] WriteHandler 실행
이름을 입력하세요: 가
/join|가
안녕
/message|안녕
/exit
19:52:25.245 [ Thread-1] 클라이언트 종료
19:52:25.246 [ Thread-1] WriteHandler 종료
19:52:25.246 [ Thread-1] ReadHandler 종료
java.net.SocketException: Socket closed
	at java.base/sun.nio.ch.NioSocketImpl.endRead(NioSocketImpl.java:243)
	at java.base/sun.nio.ch.NioSocketImpl.implRead(NioSocketImpl.java:323)
	at java.base/sun.nio.ch.NioSocketImpl.read(NioSocketImpl.java:346)
	at java.base/sun.nio.ch.NioSocketImpl$1.read(NioSocketImpl.java:796)
	at java.base/java.net.Socket$SocketInputStream.read(Socket.java:1099)
	at java.base/java.io.DataInputStream.readFully(DataInputStream.java:208)
	at java.base/java.io.DataInputStream.readUnsignedShort(DataInputStream.java:341)
	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:575)
	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:550)
	at chat.client.ReadHandler.run(ReadHandler.java:21)
	at java.base/java.lang.Thread.run(Thread.java:1583)
 */
/*
20:05:45.380 [     main] 클라이언트 시작
20:05:45.394 [ Thread-0] ReadHandler 실행
20:05:45.394 [ Thread-1] WriteHandler 실행
이름을 입력하세요: rla
rla님이 입장함
hello
[rla] hello
exit
[rla] exit
/exit
20:05:59.912 [ Thread-1] 클라이언트 종료
20:05:59.913 [ Thread-1] WriteHandler 종료
20:05:59.913 [ Thread-1] ReadHandler 종료
java.net.SocketException: Socket closed
	at java.base/sun.nio.ch.NioSocketImpl.endRead(NioSocketImpl.java:243)
	at java.base/sun.nio.ch.NioSocketImpl.implRead(NioSocketImpl.java:323)
	at java.base/sun.nio.ch.NioSocketImpl.read(NioSocketImpl.java:346)
	at java.base/sun.nio.ch.NioSocketImpl$1.read(NioSocketImpl.java:796)
	at java.base/java.net.Socket$SocketInputStream.read(Socket.java:1099)
	at java.base/java.io.DataInputStream.readFully(DataInputStream.java:208)
	at java.base/java.io.DataInputStream.readUnsignedShort(DataInputStream.java:341)
	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:575)
	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:550)
	at chat.client.ReadHandler.run(ReadHandler.java:21)
	at java.base/java.lang.Thread.run(Thread.java:1583)
 */
/*
20:07:31.200 [     main] 클라이언트 시작
20:07:31.212 [ Thread-0] ReadHandler 실행
20:07:31.212 [ Thread-1] WriteHandler 실행
이름을 입력하세요: rla님이 입장함
sk
sk님이 입장함
[rla] dkssud
gkdl
[sk] gkdl
exit
[sk] exit
/exit
20:08:15.571 [ Thread-1] 클라이언트 종료
20:08:15.571 [ Thread-1] WriteHandler 종료
20:08:15.571 [ Thread-1] ReadHandler 종료
java.net.SocketException: Socket closed
	at java.base/sun.nio.ch.NioSocketImpl.endRead(NioSocketImpl.java:243)
	at java.base/sun.nio.ch.NioSocketImpl.implRead(NioSocketImpl.java:323)
	at java.base/sun.nio.ch.NioSocketImpl.read(NioSocketImpl.java:346)
	at java.base/sun.nio.ch.NioSocketImpl$1.read(NioSocketImpl.java:796)
	at java.base/java.net.Socket$SocketInputStream.read(Socket.java:1099)
	at java.base/java.io.DataInputStream.readFully(DataInputStream.java:208)
	at java.base/java.io.DataInputStream.readUnsignedShort(DataInputStream.java:341)
	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:575)
	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:550)
	at chat.client.ReadHandler.run(ReadHandler.java:21)
	at java.base/java.lang.Thread.run(Thread.java:1583)
 */