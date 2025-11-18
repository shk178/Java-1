package lecture.demo2.web.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.domain.Member;
import lecture.demo2.domain.MemberRepository;
import lecture.demo2.web.MyView;

import java.io.IOException;
import java.util.List;

public class MemberListControllerV1 implements ControllerV1 {
    @Override
    public MyView process(HttpServletRequest req, HttpServletResponse resp) {
        MemberRepository memberRepository = MemberRepository.getInstance();
        List<Member> members = memberRepository.findAll();
        req.setAttribute("members", members);
        String viewPath = "/WEB-INF/views/list.jsp";
        return new MyView(viewPath);
    }
}
