package exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Timeout3Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        InputStream input = socket.getInputStream();
        try {
            //int read = input.read(); // 기본 - 무한으로 기다림
            socket.setSoTimeout(3000); // 타임아웃 설정
            int read = input.read();
            System.out.println(read);
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket.close();
    }
}
/*
java.net.SocketTimeoutException: Read timed out
	at java.base/sun.nio.ch.NioSocketImpl.timedRead(NioSocketImpl.java:278)
	at java.base/sun.nio.ch.NioSocketImpl.implRead(NioSocketImpl.java:304)
	at java.base/sun.nio.ch.NioSocketImpl.read(NioSocketImpl.java:346)
	at java.base/sun.nio.ch.NioSocketImpl$1.read(NioSocketImpl.java:796)
	at java.base/java.net.Socket$SocketInputStream.read(Socket.java:1099)
	at java.base/java.net.Socket$SocketInputStream.read(Socket.java:1093)
	at exception.Timeout3Client.main(Timeout3Client.java:14)
 */