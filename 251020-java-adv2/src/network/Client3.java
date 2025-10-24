package network;

import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Client3 {
    public static final int PORT = 12345;
    public static void main(String[] args) {
        System.out.println("[클라이언트 시작]");
        try (Socket socket = new Socket("localhost", PORT)) {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            // 서버 메시지 수신 스레드
            Thread receiver = new Thread(() -> {
                try {
                    while (true) {
                        String msg = input.readUTF();
                        if (msg.equals("종료")) {
                            System.out.println("\n[서버가 연결을 종료했습니다]");
                            break;
                        }
                        System.out.println("\n서버: " + msg);
                        System.out.print("전송할 내용: ");
                    }
                } catch (IOException e) {
                    System.out.println("[서버 연결 종료]");
                }
            });
            receiver.start();
            // 사용자 입력 송신 루프
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("전송할 내용: ");
                String msg = sc.nextLine();
                output.writeUTF(msg);
                if (msg.equals("종료")) {
                    System.out.println("[클라이언트 종료]");
                    break;
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
