package hello.springmvc.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {
    private final Logger log = LoggerFactory.getLogger(getClass()); // getClass() = LogController.class
    @RequestMapping("/basic/log")
    public String runLog() {
        String name = "Spring";
        log.trace("trace log={}", name);
        log.debug("debug log={}", name);
        log.info("info log={}", name);
        log.warn("warn log={}", name);
        log.error("error log={}", name);
        log.debug("String concat log=" + name);
        return "ok";
    }
}
/*
2025-11-18T21:25:34.128+09:00 DEBUG 14348 --- [springmvc] [nio-8080-exec-6] hello.springmvc.basic.LogController      : debug log=Spring
2025-11-18T21:25:34.131+09:00  INFO 14348 --- [springmvc] [nio-8080-exec-6] hello.springmvc.basic.LogController      : info log=Spring
2025-11-18T21:25:34.131+09:00  WARN 14348 --- [springmvc] [nio-8080-exec-6] hello.springmvc.basic.LogController      : warn log=Spring
2025-11-18T21:25:34.131+09:00 ERROR 14348 --- [springmvc] [nio-8080-exec-6] hello.springmvc.basic.LogController      : error log=Spring
2025-11-18T21:25:34.131+09:00 DEBUG 14348 --- [springmvc] [nio-8080-exec-6] hello.springmvc.basic.LogController      : String concat log=Spring
 */