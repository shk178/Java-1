package hello.servlet.basic;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

@WebServlet(name = "reqHeadServlet", urlPatterns = "/req-head")
public class ReqHeadServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.service(req, resp);
        printStartLine(req);
        printHeader(req);
        printHeaderUtil(req);
        printEtc(req);
    }

    private void printEtc(HttpServletRequest req) {
        System.out.println("req.getRemoteHost() = " + req.getRemoteHost());
        System.out.println("req.getRemoteAddr() = " + req.getRemoteAddr());
        System.out.println("req.getRemotePort() = " + req.getRemotePort());
        System.out.println("req.getLocalName() = " + req.getLocalName());
        System.out.println("req.getLocalAddr() = " + req.getLocalAddr());
        System.out.println("req.getLocalPort() = " + req.getLocalPort());
    }

    private void printHeaderUtil(HttpServletRequest req) {
        System.out.println("---printHeaderUtil---");
        System.out.println("req.getServerName() = " + req.getServerName());
        System.out.println("req.getServerPort() = " + req.getServerPort());
        System.out.println("req.getLocales() = " + req.getLocales());
        req.getLocales().asIterator()
                .forEachRemaining(locale -> System.out.println("locale = " + locale));
        System.out.println("req.getLocale() = " + req.getLocale());
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                System.out.print("cookie.getName() = " + cookie.getName());
                System.out.println(", cookie.getValue() = " + cookie.getValue());
            }
        }
        System.out.println("req.getContentType() = " + req.getContentType());
        System.out.println("req.getContentLength() = " + req.getContentLength());
        System.out.println("req.getCharacterEncoding() = " + req.getCharacterEncoding());
    }

    private void printHeader(HttpServletRequest req) {
        System.out.println("---printHeader---");
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + req.getHeader(headerName));
        }
        System.out.println("---printHeader (다시)---");
        req.getHeaderNames().asIterator()
                .forEachRemaining(headerName -> System.out.println(headerName + ": " + req.getHeader(headerName)));
    }

    private void printStartLine(HttpServletRequest req) {
        System.out.println("---printStartLine---");
        System.out.println("req.getMethod() = " + req.getMethod());
        System.out.println("req.getProtocol() = " + req.getProtocol());
        System.out.println("req.getScheme() = " + req.getScheme());
        System.out.println("req.getRequestURL() = " + req.getRequestURL());
        System.out.println("req.getRequestURI() = " + req.getRequestURI());
        System.out.println("req.getQueryString() = " + req.getQueryString());
        System.out.println("req.isSecure() = " + req.isSecure());
    }
}
