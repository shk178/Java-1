package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static network.MyLogger.log;

public class Http3ReqHandler implements Runnable {
    private final Socket socket;
    public Http3ReqHandler(Socket socket) {
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
             //네트워크 통신은 바이트를 주고받는다.
             //그래서 소켓이 문자 스트림을 제공하지 않는다.
             //HTTP는 텍스트 기반 프로토콜이다.
             //요청과 응답 모두 문자<->바이트 변환이 필요하다.
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
             //PrintWriter는 바이트 스트림을 받아서
             //내부적으로 OutputStreamWriter를 통해
             //문자 스트림으로 바꿔서 처리한다.
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), false, UTF_8)) {
            // 클라이언트 요청을 문자열로
            String reqString = reqToString(reader);
            if (reqString.contains("/favicon.ico")) {
                return;
            }
            // 콘솔에 요청 출력
            log(reqString);
            // 클라이언트 요청에 응답
            resToClient(reqString, writer);
            // 클라이언트에서 응답 본문 출력
        }
    }
    private String reqToString(BufferedReader reader) throws IOException {
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
    private void resToClient(String reqString, PrintWriter writer) {
        switch (reqString) {
            case String s when s.startsWith("GET /site1") -> site1(writer);
            case String s when s.startsWith("GET /site2") -> site2(writer);
            case String s when s.startsWith("GET /search") -> search(reqString, writer);
            case String s when s.startsWith("GET / ") -> home(writer);
            default -> notFound(writer);
        }
    }
    private void site1(PrintWriter writer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site1</h1>");
        String body = sb.toString();
        int contentLength = body.getBytes(UTF_8).length;
        // 헤더 (명시적으로 CRLF 사용)
        writer.print("HTTP/1.1 200 OK\r\n");
        writer.print("Content-Type: text/html; charset=UTF-8\r\n");
        writer.print("Content-Length: " + contentLength + "\r\n");
        // 공백 라인
        writer.print("\r\n");
        // 본문
        writer.print(body);
        writer.flush();
    }
    private void site2(PrintWriter writer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>site2</h1>");
        String body = sb.toString();
        int contentLength = body.getBytes(UTF_8).length;
        // 헤더 (명시적으로 CRLF 사용)
        writer.print("HTTP/1.1 200 OK\r\n");
        writer.print("Content-Type: text/html; charset=UTF-8\r\n");
        writer.print("Content-Length: " + contentLength + "\r\n");
        // 공백 라인
        writer.print("\r\n");
        // 본문
        writer.print(body);
        writer.flush();
    }
    private void search(String reqString, PrintWriter writer) {
        int startIndex = reqString.indexOf("q=");
        int endIndex = reqString.indexOf(" ", startIndex + 2);
        String query = reqString.substring(startIndex + 2, endIndex);
        String decode = URLDecoder.decode(query, UTF_8);
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>search</h1>").append("\n");
        sb.append("<ul>").append("\n");
        sb.append("<li>query: ").append(query).append("</li>").append("\n");
        sb.append("<li>decode: ").append(decode).append("</li>").append("\n");
        sb.append("</ul>");
        String body = sb.toString();
        int contentLength = body.getBytes(UTF_8).length;
        // 헤더 (명시적으로 CRLF 사용)
        writer.print("HTTP/1.1 200 OK\r\n");
        writer.print("Content-Type: text/html; charset=UTF-8\r\n");
        writer.print("Content-Length: " + contentLength + "\r\n");
        // 공백 라인
        writer.print("\r\n");
        // 본문
        writer.print(body);
        writer.flush();
    }
    private void home(PrintWriter writer) {
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
        int contentLength = body.getBytes(UTF_8).length;
        // 헤더 (명시적으로 CRLF 사용)
        writer.print("HTTP/1.1 200 OK\r\n");
        writer.print("Content-Type: text/html; charset=UTF-8\r\n");
        writer.print("Content-Length: " + contentLength + "\r\n");
        // 공백 라인
        writer.print("\r\n");
        // 본문
        writer.print(body);
        writer.flush();
    }
    private void notFound(PrintWriter writer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>404 페이지를 찾을 수 없습니다.</h1>");
        String body = sb.toString();
        int contentLength = body.getBytes(UTF_8).length;
        // 헤더 (명시적으로 CRLF 사용)
        writer.print("HTTP/1.1 404 Not Found\r\n");
        writer.print("Content-Type: text/html; charset=UTF-8\r\n");
        writer.print("Content-Length: " + contentLength + "\r\n");
        // 공백 라인
        writer.print("\r\n");
        // 본문
        writer.print(body);
        writer.flush();
    }
}
