package http;

import java.io.IOException;

public class Server1Main {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        HttpServer1 httpServer1 = new HttpServer1(PORT);
        httpServer1.start();
    }
}
// 서버 메인 실행 후 웹 브라우저 "localhost:12345" 접속
/*
21:49:14.454 [     main] [HTTP 요청]
GET / HTTP/1.1
Host: localhost:12345
Connection: keep-alive
sec-ch-ua: "Google Chrome";v="141", "Not?A_Brand";v="8", "Chromium";v="141"
sec-ch-ua-mobile: ?0
sec-ch-ua-platform: "Windows"
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,...
 */