package chat.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static network.MyLogger.log;

public class Client {
    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private ReadHandler readHandler;
    private WriteHandler writeHandler;
    private boolean closed = false;
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public void start() throws IOException {
        log("클라이언트 시작");
        socket = new Socket(host, port);
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        readHandler = new ReadHandler(input, this);
        writeHandler = new WriteHandler(output, this);
        Thread rt = new Thread(readHandler);
        Thread wt = new Thread(writeHandler);
        rt.start();
        wt.start();
    }
    public void close() {
        if (closed) {
            return;
        }
        log("클라이언트 종료");
        writeHandler.close();
        readHandler.close();
        SocketCloseUtil.closeAll(socket, input, output);
        closed = true;
    }
}
