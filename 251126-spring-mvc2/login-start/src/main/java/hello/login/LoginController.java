package hello.login;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final MemberRepository memberRepository;
    private final Map<String, Object> sessionMap = new ConcurrentHashMap<>();
    @PostMapping("/login-v1")
    public String loginV1(
            @RequestParam("loginId") String loginId,
            @RequestParam("loginPwd") String loginPwd,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1. loginId, loginPwd 확인
        Member loginMember = memberRepository.findByLoginId(loginId)
                .filter(m -> m.getLoginPwd().equals(loginPwd))
                .orElse(null);
        if (loginMember == null) {
            return "아이디 또는 비밀번호 오류입니다.";
        }
        // 2. 기존 세션 만료
        if (request.getCookies() != null) {
            Cookie findCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("sessionId"))
                    .findAny()
                    .orElse(null);
            if (findCookie != null) {
                String sessionId = findCookie.getValue();
                sessionMap.remove(sessionId);
            }
        }
        // 3. 세션
        String sessionId = UUID.randomUUID().toString();
        sessionMap.put(sessionId, loginMember);
        // 4. 쿠키
        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
        sessionIdCookie.setMaxAge(600); // 600초
        sessionIdCookie.setPath("/"); // Path 경로부터만 클라이언트가 쿠키 적용
        response.addCookie(sessionIdCookie);
        return "loginId: " + loginMember.getLoginId() + ", sessionId: " + sessionId;
    }
    @PostMapping("/login-v2")
    public String loginV2(
            @RequestParam("loginId") String loginId,
            @RequestParam("loginPwd") String loginPwd,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1. loginId, loginPwd 확인
        Member loginMember = memberRepository.findByLoginId(loginId)
                .filter(m -> m.getLoginPwd().equals(loginPwd))
                .orElse(null);
        if (loginMember == null) {
            return "아이디 또는 비밀번호 오류입니다.";
        }
        // 2. 기존 세션 만료
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        // 3. 세션
        HttpSession newSession = request.getSession();
        newSession.setAttribute("loginMember", loginMember);
        // 4. 쿠키
        /**
         * WAS가 response에 자동으로 JSESSIONID 이름을 사용해 쿠키를 넣음
         * server.servlet.session.cookie.name=MY_SESSION_ID
         */
        return "loginId: " + loginMember.getLoginId() + ", sessionId: " + newSession.getId();
    }
    @PostMapping("/logout-v1")
    public String logoutV1(HttpServletRequest request, HttpServletResponse response) {
        // 1. 기존 세션 만료
        if (request.getCookies() != null) {
            Cookie findCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("sessionId"))
                    .findAny()
                    .orElse(null);
            if (findCookie != null) {
                String sessionId = findCookie.getValue();
                sessionMap.remove(sessionId);
            }
        }
        // 2. 기존 쿠키 만료
        Cookie sessionIdCookie = new Cookie("sessionId", null);
        sessionIdCookie.setMaxAge(0); // 즉시 만료
        sessionIdCookie.setPath("/");
        response.addCookie(sessionIdCookie);
        return "로그아웃 되었습니다.";
    }
    @PostMapping("/logout-v2")
    public String logoutV2(HttpServletRequest request, HttpServletResponse response) {
        // 1. 기존 세션 만료
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        // 2. 기존 쿠키 만료
        /**
         * 톰캣이 자동으로 만료 처리한다.
         */
        return "로그아웃 되었습니다.";
    }
}
