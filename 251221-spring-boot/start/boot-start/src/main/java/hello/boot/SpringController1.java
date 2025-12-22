package hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringController1 {
    @GetMapping("/test2")
    public String run() {
        System.out.println("SpringController1.run");
        return "test2";
    }
}
/*
22-Dec-2025 13:10:40.985 정보 [http-nio-8080-exec-1] org.springframework.web.servlet.FrameworkServlet.initServletBean Initializing Servlet 'dispatcher1'
22-Dec-2025 13:10:43.136 정보 [http-nio-8080-exec-1] org.springframework.web.servlet.FrameworkServlet.initServletBean Completed initialization in 2151 ms
SpringController1.run
 */