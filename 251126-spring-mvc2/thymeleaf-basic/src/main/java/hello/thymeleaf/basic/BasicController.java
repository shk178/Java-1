package hello.thymeleaf.basic;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/basic")
public class BasicController {
    @GetMapping("/text")
    public String text(Model model) {
        model.addAttribute("text", "Hello <b>Spring</b>");
        model.addAttribute("utext", "Hello <b>Spring</b>");
        // text: 이스케이프o (<, /, > 그대로 나옴)
        // utext: 이스케이프x (Spring 진하게 나옴)
        return "basic/text";
    }
    @Data
    static class User {
        private String username;
        private int age;
    }
    @GetMapping("/variable")
    public String variable(Model model) {
        User userA = new User();
        User userB = new User();
        userA.setUsername("A");
        userB.setUsername("B");
        userA.setAge(10);
        userA.setAge(20);
        List<User> list = new ArrayList<>();
        list.add(userA);
        list.add(userB);
        Map<String, User> map = new HashMap<>();
        map.put(userA.getUsername(), userA);
        map.put(userB.getUsername(), userB);
        model.addAttribute("userA", userA);
        model.addAttribute("userList", list);
        model.addAttribute("userMap", map);
        return "basic/variable"; // Spring EL 사용
    }
    @Component("helloBean")
    static class HelloBean {
        public String hello(String data) {
            return "Hello " + data;
        }
    }
    @GetMapping("/objects")
    public String objects(
            Model model, HttpSession session,
            HttpServletRequest req, HttpServletResponse resp
    ) {
        session.setAttribute("sessionData", "Hello Session");
        model.addAttribute("req", req);
        model.addAttribute("resp", resp);
        model.addAttribute("servletContext", req.getServletContext());
        return "basic/objects";
    }
}
/* objects
http://localhost:8080/basic/objects?paramKey=value1
org.apache.catalina.connector.RequestFacade@41fdafdf
org.springframework.web.context.request.async.StandardServletAsyncWebRequest$LifecycleHttpServletResponse@cac418
org.thymeleaf.context.WebEngineContext$SessionAttributeMap@5a89b1fa
org.apache.catalina.core.ApplicationContextFacade@84861e9
ko_KR
org.thymeleaf.context.WebEngineContext$RequestParameterMap@699c4b20
value1
Hello Session
Hello Spring
 */