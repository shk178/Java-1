package http6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;
import static network.MyLogger.log;

public class Http8ReqHandler implements Runnable {
    private final Socket socket;
    private final ServletManager servletManager;
    public Http8ReqHandler(Socket socket, ServletManager servletManager) {
        this.socket = socket;
        this.servletManager = servletManager;
    }
    @Override
    public void run() {
        try {
            process(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void process(Socket socket) throws IOException {
        try (socket;
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), false, UTF_8)) {
            HttpRequest request = new HttpRequest(reader);
            HttpResponse response = new HttpResponse(writer);
            if (request.getPath().equals("/favicon.ico")) {
                return;
            }
            log(request);
            resToClient(request, response);
        }
    }
    private void resToClient(HttpRequest request, HttpResponse response) throws IOException {
        servletManager.execute(request, response);
        response.flush();
    }
}
