package hello.servlet.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

@WebServlet(name = "respBodyServlet", urlPatterns = "/resp-body")
public class RespBodyServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.service(req, resp);
        Random random = new Random();
        int number = random.nextInt(2);
        if (number == 0) {
            resp.setContentType("text/html");
            resp.setCharacterEncoding("utf-8");
            PrintWriter writer = resp.getWriter();
            writer.println("<html>");
            writer.println("<head>");
            writer.println("제목");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("내용");
            writer.println("</body>");
            writer.println("</html>");
        } else if (number == 1) {
            resp.setHeader("content-type", "application/json");
            resp.setCharacterEncoding("utf-8");
            ReqBodyServlet.HelloData data = new ReqBodyServlet.HelloData();
            data.setUsername("KIM");
            data.setAge(20);
            ObjectMapper objectMapper = new ObjectMapper();
            String result = objectMapper.writeValueAsString(data);
            resp.getWriter().write(result);
        }
    }
}
