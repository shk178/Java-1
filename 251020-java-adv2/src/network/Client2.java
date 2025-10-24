package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static network.MyLogger.log;

public class Client2 {
    public static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        log("클라이언트 시작");
        Socket socket = new Socket("localhost", PORT);
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        Scanner content = new Scanner(System.in);
        while (true) {
            System.out.print("전송할 내용: ");
            String s = content.nextLine();
            if (s.equals("종료")) break;
            output.writeUTF(s);
            System.out.println(input.readUTF());
        }
        output.close();
        input.close();
        socket.close();
        log("클라이언트 종료");
    }
}
