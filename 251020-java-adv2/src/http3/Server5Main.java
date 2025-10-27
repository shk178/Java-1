package http3;

import java.io.IOException;

public class Server5Main {
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        ServletManager servletManager = new ServletManager();
        servletManager.add("/", new HomeServlet());
        servletManager.add("/site1", new Site1Servlet());
        servletManager.add("/site2", new Site2Servlet());
        servletManager.add("/search", new SearchServlet());
        servletManager.add("/favicon.ico", new DiscardServlet());
        HttpServer5 server = new HttpServer5(PORT, servletManager);
        server.start();
    }
}
/*
20:04:52.507 [pool-1-thread-1] HttpRequest{method='GET', path='/', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,...*;q=0.8, Priority=u=0, i, User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv, Connection=keep-alive, Referer=http, Sec-Fetch-Dest=document, Sec-Fetch-Site=same-origin, Host=localhost, Accept-Encoding=gzip, deflate, br, zstd, Sec-Fetch-Mode=navigate, Upgrade-Insecure-Requests=1, Sec-Fetch-User=?1, Accept-Language=ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3}}
20:05:00.810 [pool-1-thread-3] HttpRequest{method='GET', path='/search', queryParams={q=123가나}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,...*;q=0.8, Upgrade-Insecure-Requests=1, Priority=u=0, i, User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv, Connection=keep-alive, Sec-Fetch-Dest=document, Sec-Fetch-Site=none, Host=localhost, Sec-Fetch-User=?1, Accept-Language=ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3, Accept-Encoding=gzip, deflate, br, zstd, Sec-Fetch-Mode=navigate}}
 */