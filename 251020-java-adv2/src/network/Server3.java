package network;

import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Server3 {
    private static final int PORT = 12345;
    public static void main(String[] args) {
        System.out.println("[서버 시작]");
        try (ServerSocket server = new ServerSocket(PORT)) {
            Socket socket = server.accept();
            System.out.println("[클라이언트 연결됨]");
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            // 수신 스레드 (클라이언트 메시지 수신)
            Thread receiver = new Thread(() -> {
                try {
                    while (true) {
                        String msg = input.readUTF();
                        if (msg.equals("종료")) {
                            System.out.println("\n[클라이언트가 연결을 종료했습니다]");
                            break;
                        }
                        System.out.println("\n클라이언트: " + msg);
                        System.out.print("전송할 내용: "); // 사용자 입력 안내 다시 표시
                    }
                } catch (IOException e) {
                    System.out.println("[클라이언트 연결 종료]");
                }
            });
            receiver.start();
            // 송신 루프 (서버 -> 클라이언트)
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("전송할 내용: ");
                String msg = sc.nextLine();
                output.writeUTF(msg);
                if (msg.equals("종료")) {
                    System.out.println("[서버 종료]");
                    break;
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
