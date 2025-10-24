package network4;

import network6.SessionManager;

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
        DataInputStream input = null;
        DataOutputStream output = null;
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
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
            SocketCloseUtil.closeAll(socket, input, output);
            log("연결 종료: " + socket);
        }
    }
}
