package exception;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connect {
    public static void main(String[] args) throws IOException {
        unknownHost();
        connectionRefused();
    }
    private static void unknownHost() throws IOException {
        try {
            Socket socket = new Socket("999.999.999.999", 80);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    private static void connectionRefused() throws IOException {
        try {
            Socket socket = new Socket("localhost", 45678);
            // TCP RST 패킷으로 refuse
        } catch (ConnectException e) {
            e.printStackTrace();
        }
    }
}
/*
java.net.UnknownHostException: 999.999.999.999
	at java.base/sun.nio.ch.NioSocketImpl.connect(NioSocketImpl.java:567)
	at java.base/java.net.SocksSocketImpl.connect(SocksSocketImpl.java:327)
	at java.base/java.net.Socket.connect(Socket.java:751)
	at java.base/java.net.Socket.connect(Socket.java:686)
	at java.base/java.net.Socket.<init>(Socket.java:555)
	at java.base/java.net.Socket.<init>(Socket.java:324)
	at exception.Connect.unknownHost(Connect.java:15)
	at exception.Connect.main(Connect.java:10)
java.net.ConnectException: Connection refused: connect
	at java.base/sun.nio.ch.Net.connect0(Native Method)
	at java.base/sun.nio.ch.Net.connect(Net.java:589)
	at java.base/sun.nio.ch.Net.connect(Net.java:578)
	at java.base/sun.nio.ch.NioSocketImpl.connect(NioSocketImpl.java:583)
	at java.base/java.net.SocksSocketImpl.connect(SocksSocketImpl.java:327)
	at java.base/java.net.Socket.connect(Socket.java:751)
	at java.base/java.net.Socket.connect(Socket.java:686)
	at java.base/java.net.Socket.<init>(Socket.java:555)
	at java.base/java.net.Socket.<init>(Socket.java:324)
	at exception.Connect.connectionRefused(Connect.java:22)
	at exception.Connect.main(Connect.java:11)
 */