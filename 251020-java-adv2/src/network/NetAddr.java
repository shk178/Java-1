package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetAddr {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress localhost = InetAddress.getByName("localhost");
        InetAddress google = InetAddress.getByName("google.com");
        System.out.println(localhost); // localhost/127.0.0.1
        System.out.println(google); // google.com/142.250.206.206
        // 시스템 hosts 파일 먼저 확인
        // hosts 파일에 IP 주소 없으면 DNS 서버에 요청
    }
}
