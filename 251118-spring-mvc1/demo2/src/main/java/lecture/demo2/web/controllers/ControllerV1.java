package lecture.demo2.web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.web.MyView;

import java.io.IOException;

public interface ControllerV1 {
    MyView process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
}
