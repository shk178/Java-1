package hello.hello_spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// @Controller  // 주석 처리: controller 패키지의 HomeController와 URL 매핑 충돌 방지
public class HomeControllerOld {
    @GetMapping("/")
    public String home() {
        //return "index"; // template/index.html을 렌더링
        return "redirect:/index.html"; // static/index.html로 리다이렉트
    }
}

