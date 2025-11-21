package hello.springmvc.basic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {
    @RequestMapping("/basic/hello")
    public String runHello() {
        log.info("hello");
        return "ok";
    }
    @RequestMapping(value = "/basic/hello/", method = RequestMethod.GET)
    public String getHello() {
        return "hello";
    }
    @RequestMapping("/basic/hello/{var1}/{var2}")
    public String varHello(@PathVariable String var1, @PathVariable String var2) {
        return var1 + var2;
    }
    @RequestMapping(value = "/basic/hello", params = "mode=debug")
    public String mapParams() {
        return "ok";
    }
    @RequestMapping(value = "/basic/hello", headers = "X-API-VERSION=1")
    public String mapHeaders() {
        return "ok";
    }
    @RequestMapping(value = "/basic/hello", consumes = "application/json")
    public String mapConsumes() {
        return "ok";
    }
}
