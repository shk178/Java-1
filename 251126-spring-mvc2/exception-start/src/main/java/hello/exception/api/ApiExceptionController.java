package hello.exception.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    @GetMapping("/api/ce")
    public void ce() {
        throw new CustomException();
    }
    @GetMapping("/api/rse")
    public void rse() {
        throw new ResponseStatusException(
                HttpStatus.ALREADY_REPORTED,
                "message 내용-2",
                new IllegalArgumentException()
        );
        /*
        Request URL
        http://localhost:8080/api/rse
        Request Method
        GET
        Status Code
        208 Already Reported
        Remote Address
        127.0.0.1:8080
        Referrer Policy
        strict-origin-when-cross-origin
         */
    }
    @GetMapping("/api/tme")
    public String tme(@RequestParam("data") Integer data) {
        return "ok";
        /*
        error 페이지(톰캣 400) 나온다.
         */
    }
}
