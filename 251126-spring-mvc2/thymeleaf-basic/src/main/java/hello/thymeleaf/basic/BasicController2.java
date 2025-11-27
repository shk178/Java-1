package hello.thymeleaf.basic;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/basic")
public class BasicController2 {
    @GetMapping("/date")
    public String date(Model model) {
        model.addAttribute("localDateTime", LocalDateTime.now());
        return "basic/date";
    }
    @GetMapping("/link")
    public String link(Model model) {
        model.addAttribute("param1", "data1");
        model.addAttribute("param2", "data2");
        return "basic/link";
    }
    @GetMapping("/literal")
    public String literal(Model model) {
        model.addAttribute("data", "data1");
        return "basic/literal";
        /*
        hello
        hello
        hello spring
        It's good
        It's good
        10
        3.14
        true
        false

        Hello data1
        Hello data1
         */
    }
    @GetMapping("/operation")
    public String operation(Model model) {
        model.addAttribute("num1", 1);
        model.addAttribute("num2", 2);
        model.addAttribute("bool1", true);
        model.addAttribute("bool2", false);
        model.addAttribute("null0", null);
        return "basic/operation";
        /*
        3
        true
        false
        짝수
        false
        true
        데이터 있음
         */
    }
}
