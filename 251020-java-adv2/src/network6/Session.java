package network6;

import network4.SocketCloseUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static network.MyLogger.log;

public class Session implements Runnable {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final SessionManager sessionManager;
    private boolean closed = false;
    public Session(Socket socket, SessionManager sessionManager) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        this.sessionManager = sessionManager;
        this.sessionManager.add(this);
    }
    @Override
    public void run() {
        try {
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
            sessionManager.remove(this);
            close();
        }
    }
    public synchronized void close() {
        if (closed) {
            return;
        }
        SocketCloseUtil.closeAll(socket, input, output);
        closed = true;
        log("연결 종료: " + socket);
    }
}
