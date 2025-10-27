package http;

import java.io.IOException;

public class Server3Main {
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        HttpServer3 server = new HttpServer3(PORT);
        server.start();
    }
}
/*
home
    site1
    site2
    검색어 [검색]

search
    query: 123%EA%B0%80%EB%82%98
    decode: 123가나

404 페이지를 찾을 수 없습니다.
 */
/*
17:45:09.739 [pool-1-thread-1] GET / HTTP/1.1
Host: localhost:12345
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,...

17:45:16.116 [pool-1-thread-2] GET /site1 HTTP/1.1
Host: localhost:12345
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,...

17:45:19.046 [pool-1-thread-3] GET /site2 HTTP/1.1
Host: localhost:12345
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,...

17:45:24.236 [pool-1-thread-4] GET /search?q=hello HTTP/1.1
Host: localhost:12345
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,...

17:45:32.340 [pool-1-thread-5] GET /search?q=123%EA%B0%80%EB%82%98 HTTP/1.1
Host: localhost:12345
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,...

17:46:01.244 [pool-1-thread-6] GET / HTTP/1.1
Host: localhost:12345
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,...

17:46:28.131 [pool-1-thread-7] GET /1 HTTP/1.1
Host: localhost:12345
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:144.0) Gecko/20100101 Firefox/144.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,...
 */