package http2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer4 {
    private final ExecutorService es = Executors.newFixedThreadPool(10);
    private final int port;
    public HttpServer4(int port) {
        this.port = port;
    }
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            es.submit(new Http4ReqHandler(socket));
        }
    }
}
