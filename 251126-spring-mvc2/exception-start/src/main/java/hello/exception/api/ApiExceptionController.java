package hello.exception.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiExceptionController {
    @GetMapping("/members/{id}")
    public MemberDto getMember(@PathVariable("id") String id) {
        if (id.equals("err")) {
            throw new RuntimeException("멤버 에러");
        }
        return new MemberDto(id, "name-" + id);
    }
    @GetMapping("/members/iae")
    public void iae() {
        throw new IllegalArgumentException("상태 코드 설정 실습");
        /* MyHandlerExceptionResolver
        {
            "timestamp": "2025-12-01T08:06:57.234+00:00",
            "status": 400,
            "error": "Bad Request",
            "path": "/members/iae"
        }
         */
    }
    @GetMapping("/members/ue")
    public void ue() {
        throw new UserException("유저 오류");
    }
}
