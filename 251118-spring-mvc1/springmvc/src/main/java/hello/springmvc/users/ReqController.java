package hello.springmvc.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users/req")
public class ReqController {
    @RequestMapping("/headers")
    public String headers(
            HttpServletRequest req, HttpServletResponse resp,
            HttpMethod method, Locale locale,
            @RequestHeader MultiValueMap<String, String> headerMap, @RequestHeader("host") String headerHost,
            @CookieValue(value = "myCookie", required = false) String myCookie
    ) {
        log.info("req={}", req);
        log.info("resp={}", resp);
        log.info("method={}", method);
        log.info("locale={}", locale);
        log.info("headerMap={}", headerMap);
        log.info("headerHost={}", headerHost);
        log.info("myCookie={}", myCookie);
        return "ok";
    }
    @RequestMapping("/params-v1")
    public void paramsV1(
            HttpServletRequest req, HttpServletResponse resp
    ) throws IOException {
        String username = req.getParameter("username");
        Integer age = Integer.parseInt(req.getParameter("age"));
        log.info("username={}, age={}", username, age);
        resp.getWriter().write("ok");
    }
    @ResponseBody
    @RequestMapping("/params-v2")
    public String paramsV2(
            @RequestParam("username") String username, @RequestParam("age") Integer age
    ) {
        log.info("username={}, age={}", username, age);
        return "ok";
    }
    @ResponseBody
    @RequestMapping("/params-v3")
    public String paramsV3(
            String username, Integer age
            /* @RequestParam + required=false 적용 */
    ) {
        log.info("username={}, age={}", username, age);
        return "ok";
    }
    @ResponseBody
    @RequestMapping("/params-v4")
    public String paramsV4(
            @RequestParam(defaultValue = "user1") String username,
            @RequestParam(required = false, defaultValue = "1") Integer age
    ) {
        log.info("username={}, age={}", username, age);
        return "ok";
    }
    @ResponseBody
    @RequestMapping("/params-v5")
    public String paramsV5(
            @RequestParam Map<String, Object> paramMap
            /* 값이 여러 개면 MultiValueMap 사용 */
            ) {
        log.info("username={}, age={}", paramMap.get("username"), paramMap.get("age"));
        return "ok";
    }
    @ResponseBody
    @RequestMapping("/params-v6")
    public String paramsV6(
            @ModelAttribute HelloData helloData
    ) {
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return "ok";
    }
    @ResponseBody
    @RequestMapping("/params-v7")
    public String paramsV7(
            HelloData helloData
            /* @ModelAttribute 적용 */
    ) {
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return "ok";
    }
}
