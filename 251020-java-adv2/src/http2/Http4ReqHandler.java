package http2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static network.MyLogger.log;

public class Http4ReqHandler implements Runnable {
    private final Socket socket;
    public Http4ReqHandler(Socket socket) {
        this.socket = socket;
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
            resToClient(request, response, writer);
        }
    }
    private void resToClient(HttpRequest request, HttpResponse response, PrintWriter writer) {
        switch (request.getPath()) {
            case "/site1" -> site1(response, writer);
            case "/site2" -> site2(response, writer);
            case "/search" -> search(request, response, writer);
            case "/" -> home(response, writer);
            default -> notFound(response, writer);
        }
        response.flush();
    }
    private void site1(HttpResponse response, PrintWriter writer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site1</h1>");
        String body = sb.toString();
        response.writeBody(body);
    }
    private void site2(HttpResponse response, PrintWriter writer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site2</h1>");
        String body = sb.toString();
        response.writeBody(body);
    }
    private void search(HttpRequest request, HttpResponse response, PrintWriter writer) {
        String query = request.getQueryParam("q"); // private void parseQueryParams(String query)에서 디코드되어 있다.
        String decode = URLDecoder.decode(query, UTF_8);
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>search</h1>").append("\n");
        sb.append("<ul>").append("\n");
        sb.append("<li>query: ").append(query).append("</li>").append("\n");
        sb.append("<li>decode: ").append(decode).append("</li>").append("\n");
        sb.append("</ul>");
        String body = sb.toString();
        response.writeBody(body);
    }
    private void home(HttpResponse response, PrintWriter writer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>home</h1>").append("\n");
        sb.append("<ul>").append("\n");
        sb.append("<li><a href='/site1'>site1</a></li>").append("\n");
        sb.append("<li><a href='/site2'>site2</a></li>").append("\n");
        sb.append("<li><form action='/search' method='GET'>");
        sb.append("<input type='text' name='q' placeholder='검색어'>");
        sb.append("<button type='submit'>검색</button>");
        sb.append("</form></li>").append("\n");
        sb.append("</ul>");
        String body = sb.toString();
        response.writeBody(body);
    }
    private void notFound(HttpResponse response, PrintWriter writer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>404 페이지를 찾을 수 없습니다.</h1>");
        String body = sb.toString();
        response.writeBody(body);
        response.setStatusCode(404);
    }
}
