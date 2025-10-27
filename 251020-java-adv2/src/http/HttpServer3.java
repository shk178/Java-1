package http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer3 {
    private final ExecutorService es = Executors.newFixedThreadPool(10);
    private final int port;
    public HttpServer3(int port) {
        this.port = port;
    }
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            es.submit(new Http3ReqHandler(socket));
        }
    }
}
