package hello.login.web.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
public class SessionInfoController {
    @GetMapping("/session-info")
    public String sessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "세션이 없습니다.";
        }
        session.getAttributeNames().asIterator()
                .forEachRemaining(name -> log.info("session name={}, value={}", name, session.getAttribute(name)));
        log.info("session Id={}", session.getId());
        log.info("MaxInactiveInterval={}", session.getMaxInactiveInterval());
        log.info("CreationTime={}", session.getCreationTime());
        log.info("LastAccessedTime={}", session.getLastAccessedTime());
        log.info("isNew={}", session.isNew());
        return "정보가 출력되었습니다.";
    }
}
