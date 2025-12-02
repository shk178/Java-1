package hello.exception.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.BAD_GATEWAY, reason="message 내용")
public class CustomException extends RuntimeException {
}
