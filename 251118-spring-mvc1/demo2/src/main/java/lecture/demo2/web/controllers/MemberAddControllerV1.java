package lecture.demo2.web.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.web.MyView;

import java.io.IOException;

public class MemberAddControllerV1 implements ControllerV1 {
    @Override
    public MyView process(HttpServletRequest req, HttpServletResponse resp) {
        String viewPath = "/WEB-INF/views/add.jsp";
        return new MyView(viewPath);
    }
}
