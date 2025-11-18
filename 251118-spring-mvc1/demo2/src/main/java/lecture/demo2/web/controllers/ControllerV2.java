package lecture.demo2.web.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.web.ModelView;
import lecture.demo2.web.MyView;

import java.io.IOException;
import java.util.Map;

public interface ControllerV2 {
    ModelView process(Map<String, String> paramMap);
}
