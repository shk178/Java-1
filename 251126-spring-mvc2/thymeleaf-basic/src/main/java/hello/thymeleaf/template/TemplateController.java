package hello.thymeleaf.template;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/template")
public class TemplateController {
    @GetMapping("/fragment")
    public String fragment() {
        return "template/fragment/fragmentMain";
        /*
        푸터
        푸터
        푸터
        파라미터

        데이터1

        데이터2
         */
    }
    @GetMapping("/layout")
    public String layout() {
        return "template/layout/layoutMain";
    }
    @GetMapping("/layout2")
    public String layout2() {
        return "template/layout/extendMain";
        /*
        H1

        레이아웃 컨텐츠 다시

        레이아웃 푸터
         */
    }
}
