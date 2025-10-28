package http5;

import java.io.IOException;
import java.util.List;

public class Server7Main {
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        List<Object> controllers = List.of(new SiteController());
        HttpServlet annotationServlet = new AnnotationServlet(controllers);
        ServletManager servletManager = new ServletManager();
        servletManager.setDefaultServlet(annotationServlet);
        servletManager.add("/favicon.ico", new DiscardServlet());
        HttpServer7 server = new HttpServer7(PORT, servletManager);
        server.start();
    }
}
/*
11:38:41.597 [pool-1-thread-1] HttpRequest{method='GET', path='/', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,...*;q=0.8, Priority=u=0, i, User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv, Connection=keep-alive, Referer=http, Sec-Fetch-Dest=document, Sec-Fetch-Site=same-origin, Host=localhost, Accept-Encoding=gzip, deflate, br, zstd, Sec-Fetch-Mode=navigate, Upgrade-Insecure-Requests=1, Sec-Fetch-User=?1, Accept-Language=ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3}}
11:38:45.629 [pool-1-thread-3] HttpRequest{method='GET', path='/site2', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,...*;q=0.8, Priority=u=0, i, User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv, Connection=keep-alive, Referer=http, Sec-Fetch-Dest=document, Sec-Fetch-Site=same-origin, Host=localhost, Accept-Encoding=gzip, deflate, br, zstd, Sec-Fetch-Mode=navigate, Upgrade-Insecure-Requests=1, Sec-Fetch-User=?1, Accept-Language=ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3}}
11:38:50.754 [pool-1-thread-5] HttpRequest{method='GET', path='/2', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,...
 */