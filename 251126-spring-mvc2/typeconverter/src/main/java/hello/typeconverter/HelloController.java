package hello.typeconverter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {
        String data = request.getParameter("data");
        Integer number = Integer.valueOf(data) * 2;
        return number.toString();
    }
    @GetMapping("/convert-1")
    public One convert1(@RequestParam One one) {
        return one;
        // http://localhost:8080/convert-1?one=oneInt:1,oneInteger:10,oneString:ABC
        // {"oneInt":1,"oneInteger":10,"oneString":"ABC"}
    }
    @GetMapping("/convert-2/{one}")
    public One convert2(@PathVariable One one) {
        return one;
        // http://localhost:8080/convert-2/oneInt:1,oneInteger:10,oneString:ABC
        // {"oneInt":1,"oneInteger":10,"oneString":"ABC"}
    }
    @GetMapping("/convert-3")
    public One convert3(@ModelAttribute One one) {
        return one;
        // http://localhost:8080/convert-3?oneInt=1&oneInteger=10&oneString=ABC
        // {"oneInt":1,"oneInteger":10,"oneString":"ABC"}
        // http://localhost:8080/convert-3?oneInt=1&oneInteger=1,000&oneString=ABC
        // {"oneInt":1,"oneInteger":1000,"oneString":"ABC"}
    }
}
