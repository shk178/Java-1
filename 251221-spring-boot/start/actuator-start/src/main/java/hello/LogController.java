package hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class LogController {
    @GetMapping("/log")
    public String log() {
        log.trace("로그t");
        log.debug("로그d");
        log.info("로그i");
        log.warn("로그w");
        log.error("로그e");
        return "ok";
    }
}
