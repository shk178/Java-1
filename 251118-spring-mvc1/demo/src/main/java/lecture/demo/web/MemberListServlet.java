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
import java.util.List;

@WebServlet(name = "memberListServlet", urlPatterns = "/view")
public class MemberListServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MemberRepository memberRepository = MemberRepository.getInstance();
        List<Member> members = memberRepository.findAll();
        req.setAttribute("members", members);
        String viewPath = "/WEB-INF/views/view.jsp";
        RequestDispatcher rd = req.getRequestDispatcher(viewPath);
        rd.forward(req, resp);
    }
}