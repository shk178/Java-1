package lecture.demo.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo.domain.Member;
import lecture.demo.domain.MemberRepository;

import java.io.IOException;

@WebServlet(name = "memberSaveServlet", urlPatterns = "/save")
public class MemberSaveServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MemberRepository memberRepository = MemberRepository.getInstance();
        String username = req.getParameter("username");
        int age = Integer.parseInt(req.getParameter("age"));
        Member member = new Member(username, age);
        memberRepository.save(member);
        req.setAttribute("member", member);
        String viewPath = "/WEB-INF/views/save.jsp";
        RequestDispatcher rd = req.getRequestDispatcher(viewPath);
        rd.forward(req, resp);
    }
}