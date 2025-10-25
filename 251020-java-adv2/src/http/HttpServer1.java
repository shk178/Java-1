package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static network.MyLogger.log;

public class HttpServer1 {
    private final int port;
    public HttpServer1(int port) {
        this.port = port;
    }
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            process(socket);
        }
    }
    private void process(Socket socket) {
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), false, StandardCharsets.UTF_8)
             ){
            String reqStr = requestToString(reader);
            log("[HTTP 요청]\n" + reqStr);
            if (reqStr.contains("/favicon.ico")) {
                return;
            }
            sleep();
            respondToClient(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void sleep() {
        try {
            Thread.sleep(5000); // 서버 처리 시간
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private static void respondToClient(PrintWriter writer) {
        String body = "<h1>Hello</h1>";
        int length = body.getBytes(StandardCharsets.UTF_8).length;
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        sb.append("Content-Type: text/html\r\n");
        sb.append("Content-Length: ").append(length).append("\r\n");
        sb.append("\r\n"); // 공백 라인
        sb.append(body);
        writer.println("[HTTP 응답]\n" + sb);
        writer.flush();
    }
    private static String requestToString(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
