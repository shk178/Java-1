package network5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static network.MyLogger.log;

public class Server {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            log("소켓 연결: " + socket);
            Session session = new Session(socket);
            Thread thread = new Thread(session);
            thread.start();
        }
    }
}
