package http6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer8 {
    private final ExecutorService es = Executors.newFixedThreadPool(10);
    private final int port;
    private final ServletManager servletManager;
    public HttpServer8(int port, ServletManager servletManager) {
        this.port = port;
        this.servletManager = servletManager;
    }
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            es.submit(new Http8ReqHandler(socket, servletManager));
        }
    }
}
