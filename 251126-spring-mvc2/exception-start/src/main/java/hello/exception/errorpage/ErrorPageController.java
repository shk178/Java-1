package hello.exception.errorpage;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ErrorPageController {
    @RequestMapping("/error-page/Re")
    public String secondRe(HttpServletRequest request) {
        System.out.println(this.getClass() + ".secondRe");
        //Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        //System.out.println("exception = " + exception);
        System.out.println("message = " + message);
        System.out.println("uri = " + uri);
        System.out.println("status = " + status);
        return "Re";
    }
    @RequestMapping(value = "/error-page/Re", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> secondReApi(HttpServletRequest request) {
        System.out.println(this.getClass() + ".secondReApi");
        //Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Map<String, Object> result = new HashMap<>();
        //result.put("exception", exception);
        result.put("message", message);
        result.put("uri", uri);
        result.put("status", status);
        return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) status));
    }
    @RequestMapping("/error-page/404")
    public String second404() {
        System.out.println(this.getClass() + ".second404");
        return "404";
    }
}
