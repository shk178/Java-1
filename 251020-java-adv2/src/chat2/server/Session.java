package chat2.server;

import chat2.client.SocketCloseUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static network.MyLogger.log;

public class Session implements Runnable {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final CommandManager commandManager;
    private final SessionManager sessionManager;
    private boolean closed = false;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Session(Socket socket, CommandManager commandManager, SessionManager sessionManager) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        this.commandManager = commandManager;
        this.sessionManager = sessionManager;
        this.sessionManager.add(this);
    }
    @Override
    public void run() {
        log("서버 세션 실행");
        try {
            while (true) {
                String received = input.readUTF();
                commandManager.execute(received, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sessionManager.remove(this);
            sessionManager.sendAll(username + "님이 퇴장함");
            close();
        }
    }
    public void send(String message) throws IOException {
        output.writeUTF(message);
    }
    public synchronized void close() {
        if (closed) {
            return;
        }
        log("서버 세션 종료");
        SocketCloseUtil.closeAll(socket, input, output);
        closed = true;
    }
}
