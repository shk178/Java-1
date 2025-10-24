package network6;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static network.MyLogger.log;

public class Client {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("전송 문자: ");
                String toSend = scanner.nextLine();
                output.writeUTF(toSend);
                if (toSend.equals("exit")) {
                    break;
                }
                String received = input.readUTF();
                System.out.println("받은 문자: " + received);
            }
        } catch (IOException e) {
            log(e);
        }
    }
}
