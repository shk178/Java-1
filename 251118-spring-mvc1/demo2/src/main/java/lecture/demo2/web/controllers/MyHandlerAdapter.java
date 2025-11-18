package lecture.demo2.web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.web.ModelView;

import java.io.IOException;

public interface MyHandlerAdapter {
    boolean supports(Object handler);
    ModelView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws ServletException, IOException;
}
