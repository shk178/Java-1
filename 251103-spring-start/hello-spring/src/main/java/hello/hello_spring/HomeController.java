package hello.hello_spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        //return "index"; // template/index.html을 렌더링
        return "redirect:/index.html"; // static/index.html로 리다이렉트
    }
}