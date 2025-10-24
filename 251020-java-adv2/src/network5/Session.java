package network5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static network.MyLogger.log;

public class Session implements Runnable {
    private final Socket socket;
    public Session(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            while (true) {
                String received = input.readUTF();
                System.out.println("받은 문자: " + received);
                if (received.equals("exit")) {
                    break;
                }
                String toSend = received + " World";
                output.writeUTF(toSend);
            }
        } catch (IOException e) {
            log(e);
        } finally {
            log("연결 종료: " + socket);
        }
    }
}
