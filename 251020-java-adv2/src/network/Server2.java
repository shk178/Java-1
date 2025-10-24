package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static network.MyLogger.log;

public class Server2 {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        log("서버 시작");
        ServerSocket server = new ServerSocket(PORT);
        Socket socket = server.accept();
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        Scanner content = new Scanner(System.in);
        while (true) {
            System.out.println(input.readUTF());
            System.out.print("전송할 내용: ");
            String s = content.nextLine();
            if (s.equals("종료")) break;
            output.writeUTF(s);
        }
        output.close();
        input.close();
        socket.close();
        server.close();
        log("서버 종료");
    }
}
