package lecture.demo2.web.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.domain.Member;
import lecture.demo2.domain.MemberRepository;
import lecture.demo2.web.MyView;

import java.io.IOException;

public class MemberSaveControllerV1 implements ControllerV1 {
    @Override
    public MyView process(HttpServletRequest req, HttpServletResponse resp) {
        MemberRepository memberRepository = MemberRepository.getInstance();
        String username = req.getParameter("username");
        int age = Integer.parseInt(req.getParameter("age"));
        Member member = new Member(username, age);
        memberRepository.save(member);
        req.setAttribute("member", member);
        String viewPath = "/WEB-INF/views/save.jsp";
        return new MyView(viewPath);
    }
}
