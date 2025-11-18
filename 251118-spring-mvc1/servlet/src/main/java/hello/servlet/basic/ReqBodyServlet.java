package hello.servlet.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "reqBodyServlet", urlPatterns = "/req-body")
public class ReqBodyServlet extends HttpServlet {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // super.service(req, resp);
        System.out.println("req.getContentType() = " + req.getContentType());
        if (req.getContentType().equals("text/plain")) {
            ServletInputStream inputStream = req.getInputStream();
            String msgBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            System.out.println("msgBody = " + msgBody);
        } else if (req.getContentType().equals("application/json")) {
            ServletInputStream inputStream = req.getInputStream();
            String msgBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            System.out.println("msgBody = " + msgBody);
            HelloData helloData = objectMapper.readValue(msgBody, HelloData.class);
            System.out.println("helloData = " + helloData);
        }
    }
    static class HelloData {
        private String username;
        private int age;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "HelloData{" +
                    "username='" + username + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
/*
req.getContentType() = application/json
msgBody = {"username": "hello", "age": 20}
helloData = HelloData{username='hello', age=20}
req.getContentType() = text/plain
msgBody = 123
 */