package exception;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ResetCloseClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        Socket socket = new Socket("localhost", 12345);
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        Thread.sleep(1000); // 서버가 close() 할 때까지 대기
        output.write(1); // 서버가 FIN 보냈는데 클라이언트가 write()
        Thread.sleep(1000); // 서버가 RST 보낼 때까지 대기
        try {
            int read = input.read();
            System.out.println(read);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
/*
java.net.SocketException: 현재 연결은 사용자의 호스트 시스템의 소프트웨어의 의해 중단되었습니다
	at java.base/sun.nio.ch.SocketDispatcher.read0(Native Method)
	at java.base/sun.nio.ch.SocketDispatcher.read(SocketDispatcher.java:46)
	at java.base/sun.nio.ch.NioSocketImpl.tryRead(NioSocketImpl.java:256)
	at java.base/sun.nio.ch.NioSocketImpl.implRead(NioSocketImpl.java:307)
	at java.base/sun.nio.ch.NioSocketImpl.read(NioSocketImpl.java:346)
	at java.base/sun.nio.ch.NioSocketImpl$1.read(NioSocketImpl.java:796)
	at java.base/java.net.Socket$SocketInputStream.read(Socket.java:1099)
	at java.base/java.net.Socket$SocketInputStream.read(Socket.java:1093)
	at exception.ResetCloseClient.main(ResetCloseClient.java:18)
 */
/*
//output.write(1); 하지 않으면 -1만 출력됨
 */