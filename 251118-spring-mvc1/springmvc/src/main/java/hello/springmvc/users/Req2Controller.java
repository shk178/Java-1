package hello.springmvc.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequestMapping("/users/req2")
public class Req2Controller {
    @PostMapping("/body-v1")
    public void bodyV1(
            HttpServletRequest req, HttpServletResponse resp
    ) throws IOException {
        ServletInputStream inputStream = req.getInputStream();
        String msgBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        log.info("msgBody={}", msgBody);
        resp.getWriter().write("ok");
    }
    @PostMapping("/body-v2")
    public void bodyV2(
            InputStream inputStream, Writer respWriter
    ) throws IOException {
        String msgBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        log.info("msgBody={}", msgBody);
        respWriter.write("ok");
    }
    @PostMapping("/body-v3")
    public HttpEntity<String> bodyV3(
            HttpEntity<String> httpEntity
    ) throws IOException {
        String msgBody = httpEntity.getBody();
        log.info("msgBody={}", msgBody);
        return new HttpEntity<>("ok");
    }
    @ResponseBody
    @PostMapping("/body-v4")
    public String bodyV4(
            @RequestBody String msgBody
    ) throws IOException {
        log.info("msgBody={}", msgBody);
        return "ok";
    }
    @PostMapping("/body-v5")
    public void bodyV5(
            HttpServletRequest req, HttpServletResponse resp
    ) throws IOException {
        ServletInputStream inputStream = req.getInputStream();
        String msgBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        log.info("msgBody={}", msgBody);
        HelloData helloData = new ObjectMapper().readValue(msgBody, HelloData.class);
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        resp.getWriter().write("ok");
    }
    @ResponseBody
    @PostMapping("/body-v6")
    public String bodyV6(
            @RequestBody String msgBody
    ) throws JsonProcessingException {
        log.info("msgBody={}", msgBody);
        HelloData helloData = new ObjectMapper().readValue(msgBody, HelloData.class);
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return "ok";
    }
    @ResponseBody
    @PostMapping("/body-v7")
    public String bodyV7(
            @RequestBody HelloData helloData
    ) {
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return "ok";
    }
    @ResponseBody
    @PostMapping("/body-v8")
    public String bodyV8(
            HttpEntity<HelloData> httpEntity
    ) {
        HelloData helloData = httpEntity.getBody();
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return "ok";
    }
    @ResponseBody
    @PostMapping("/body-v9")
    public HelloData bodyV9(
            @RequestBody HelloData helloData
    ) {
        log.info("username={}, age={}", helloData.getUsername(), helloData.getAge());
        return helloData;
    }
}
