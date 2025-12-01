package hello.exception.errorpage;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class ServletExController {
    @GetMapping("/error-re")
    public void firstRe() {
        System.out.println(this.getClass() + ".firstRe");
        throw new RuntimeException("런타임 에러 발생");
    }
    @GetMapping("/error-404")
    public void first404(HttpServletResponse response) throws IOException {
        System.out.println(this.getClass() + ".first404");
        response.sendError(404, "404 오류 요청");
    }
}
