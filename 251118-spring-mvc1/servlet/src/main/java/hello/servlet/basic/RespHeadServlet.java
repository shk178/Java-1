package hello.servlet.basic;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "respHeadServlet", urlPatterns = "/resp-head")
public class RespHeadServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.service(req, resp);
        /* start line */
        resp.setStatus(HttpServletResponse.SC_OK);
        /* header */
        resp.setHeader("Content-Type", "text/plain;charset=utf-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("my-header", "hello");
        /* header util */
        content(resp);
        cookie(resp);
        redirect(resp);
        /* msg body */
        PrintWriter writer = resp.getWriter();
        writer.println("hello kim");
    }

    private void redirect(HttpServletResponse resp) throws IOException {
        // resp.sendRedirect("/basic/hello-form.html");
    }

    private void cookie(HttpServletResponse resp) {
        Cookie cookie = new Cookie("myCookie", "good");
        cookie.setMaxAge(600);
        resp.addCookie(cookie);
    }

    private void content(HttpServletResponse resp) {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("utf-8");
        // setContentLength 생략 시 자동 생성
    }
}
