package hello.servlet.basic;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;

@WebServlet(name = "reqParamServlet", urlPatterns = "/req-param")
public class ReqParamServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.service(req, resp);
        req.getParameterNames().asIterator()
                .forEachRemaining(paramName -> {
                    System.out.print("paramName = " + paramName);
                    System.out.print(", req.getParameter(paramName) = " + req.getParameter(paramName));
                    System.out.println(", req.getParameterValues(paramName) = " + req.getParameterValues(paramName));
                    System.out.println(Arrays.toString(req.getParameterValues(paramName)));
                });
    }
}
/*
paramName = nameA, req.getParameter(paramName) = valueA, req.getParameterValues(paramName) = [Ljava.lang.String;@76c30793
[valueA, valueC]
paramName = nameB, req.getParameter(paramName) = valueB, req.getParameterValues(paramName) = [Ljava.lang.String;@479352b4
[valueB]
 */
/*
paramName = username, req.getParameter(paramName) = kim, req.getParameterValues(paramName) = [Ljava.lang.String;@80910b8
[kim]
paramName = age, req.getParameter(paramName) = 13, req.getParameterValues(paramName) = [Ljava.lang.String;@50421630
[13]
 */