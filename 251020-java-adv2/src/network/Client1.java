package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static network.MyLogger.log;

public class Client1 {
    private static final int PORT = 12345; // 서버 포트
    // 클라이언트 포트는 자동 할당된다.
    // 서버 프로그램 실행 중 클라이언트가 연결 요청 보내야 한다.
    public static void main(String[] args) throws IOException {
        log("클라이언트 시작");
        Socket socket = new Socket("localhost", PORT);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        output.writeUTF("Hello");
        System.out.println(input.readUTF());
        output.close();
        input.close();
        socket.close();
        log("클라이언트 종료");
    }
}
