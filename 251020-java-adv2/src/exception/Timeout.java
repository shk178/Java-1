package exception;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class Timeout {
    public static void main(String[] args) throws IOException {
        long sTime = System.currentTimeMillis();
        try {
            Socket socket = new Socket("192.168.1.250", 45678);
            // OS마다 연결 대기하는 타임아웃이 있다.
        } catch (ConnectException e) {
            e.printStackTrace();
        }
        long eTime = System.currentTimeMillis();
        System.out.println(eTime - sTime);
    }
}
/*
java.net.ConnectException: Connection timed out: connect
	at java.base/sun.nio.ch.Net.connect0(Native Method)
	at java.base/sun.nio.ch.Net.connect(Net.java:589)
	at java.base/sun.nio.ch.Net.connect(Net.java:578)
	at java.base/sun.nio.ch.NioSocketImpl.connect(NioSocketImpl.java:583)
	at java.base/java.net.SocksSocketImpl.connect(SocksSocketImpl.java:327)
	at java.base/java.net.Socket.connect(Socket.java:751)
	at java.base/java.net.Socket.connect(Socket.java:686)
	at java.base/java.net.Socket.<init>(Socket.java:555)
	at java.base/java.net.Socket.<init>(Socket.java:324)
	at exception.Timeout.main(Timeout.java:11)
21042
 */