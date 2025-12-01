package hello.exception.errorpage;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController {
    @RequestMapping("/error-page/Re")
    public String secondRe(HttpServletRequest request) {
        System.out.println(this.getClass() + ".secondRe");
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        System.out.println("exception = " + exception);
        System.out.println("message = " + message);
        System.out.println("uri = " + uri);
        System.out.println("status = " + status);
        return "Re";
    }
    @RequestMapping("/error-page/404")
    public String second404() {
        System.out.println(this.getClass() + ".second404");
        return "404";
    }
}
