package hello.springmvc.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/users/resp")
public class RespController {
    @GetMapping("/v1")
    public void v1(HttpServletResponse resp) throws IOException {
        resp.getWriter().write("ok");
    }
    @GetMapping("/v2")
    public ResponseEntity<String> v2() throws IOException {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }
    @ResponseBody
    @GetMapping("/v3")
    public String v3() {
        return "ok";
    }
    @GetMapping("/v4")
    public ResponseEntity<HelloData> v4() {
        HelloData helloData = new HelloData();
        helloData.setUsername("user1");
        helloData.setAge(1);
        return new ResponseEntity<>(helloData, HttpStatus.OK);
    }
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @GetMapping("/v5")
    public HelloData v5() {
        HelloData helloData = new HelloData();
        helloData.setUsername("user1");
        helloData.setAge(1);
        return helloData;
    }
    /* HttpMessageConverter<T> 인터페이스 사용
    canRead(), canWrite(): 컨버터가 해당 클래스 및 미디어타입을 지원하는지 체크한다.
    read(), write(): 컨버터로 메시지 읽고 쓴다.
     */
    /* 스프링 부트 기본 메시지 컨버터
    0 = ByteArrayHttpMessageConverter (byte[] 클래스 , 미디어타입은 전체)
    1 = StringHttpMessageConverter (String 클래스, 미디어타입은 전체)
    2 = MappingJackson2HttpMessageConverter (객체 또는 HashMap, 미디어타입은 application/json 관련)
     */
}
