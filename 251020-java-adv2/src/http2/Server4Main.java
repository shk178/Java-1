package http2;

import java.io.IOException;

public class Server4Main {
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        HttpServer4 server = new HttpServer4(PORT);
        server.start();
    }
}
/*
search
    query: 123가나다
    decode: 123가나다

19:06:45.948 [pool-1-thread-1] HttpRequest{method='GET', path='/', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,.../*;q=0.8, Priority=u=0, i, User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv, Connection=keep-alive, Referer=http, Sec-Fetch-Dest=document, Sec-Fetch-Site=same-origin, Host=localhost, Accept-Encoding=gzip, deflate, br, zstd, Sec-Fetch-Mode=navigate, Upgrade-Insecure-Requests=1, Sec-Fetch-User=?1, Accept-Language=ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3}}
19:06:50.804 [pool-1-thread-3] HttpRequest{method='GET', path='/site2', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,.../*;q=0.8, Priority=u=0, i, User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv, Connection=keep-alive, Referer=http, Sec-Fetch-Dest=document, Sec-Fetch-Site=same-origin, Host=localhost, Accept-Encoding=gzip, deflate, br, zstd, Sec-Fetch-Mode=navigate, Upgrade-Insecure-Requests=1, Sec-Fetch-User=?1, Accept-Language=ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3}}
 */