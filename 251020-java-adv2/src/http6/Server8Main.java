package http6;

import java.io.IOException;
import java.util.List;

public class Server8Main {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        MemberRepository memberRepository = new MemberRepository();
        MemberController memberController = new MemberController(memberRepository);
        List<Object> controllers = List.of(memberController);
        HttpServlet annotationServlet = new AnnotationServlet(controllers);
        ServletManager servletManager = new ServletManager();
        servletManager.setDefaultServlet(annotationServlet);
        servletManager.add("/favicon.ico", new DiscardServlet());
        HttpServer8 server = new HttpServer8(PORT, servletManager);
        server.start();
    }
}
/*
13:12:09.802 [pool-1-thread-1] HttpRequest{method='GET', path='/', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,...*;q=0.8, Priority=u=0, i, User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv, Connection=keep-alive, Referer=http, Sec-Fetch-Dest=document, Sec-Fetch-Site=same-origin, Host=localhost, Accept-Encoding=gzip, deflate, br, zstd, Sec-Fetch-Mode=navigate, Upgrade-Insecure-Requests=1, Sec-Fetch-User=?1, Accept-Language=ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3}}
13:12:12.322 [pool-1-thread-3] HttpRequest{method='GET', path='/add-member-form', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,...*;q=0.8, Priority=u=0, i, User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv, Connection=keep-alive, Referer=http, Sec-Fetch-Dest=document, Sec-Fetch-Site=same-origin, Host=localhost, Accept-Encoding=gzip, deflate, br, zstd, Sec-Fetch-Mode=navigate, Upgrade-Insecure-Requests=1, Sec-Fetch-User=?1, Accept-Language=ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3, Content-Length=17, Content-Type=application/x-www-form-urlencoded}}
회원이 등록되었습니다.
13:12:17.633 [pool-1-thread-5] HttpRequest{method='GET', path='/members', queryParams={}, headers={Accept=text/html,application/xhtml+xml,application/xml;q=0.9,...
 */