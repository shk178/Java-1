package hello.exception.api2;

import hello.exception.api.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class Api2ExceptionController {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ErrorResult one(Exception e) {
        return new ErrorResult("1", e.getMessage());
    }
    @ExceptionHandler
    public ResponseEntity<ErrorResult> two(UserException e) {
        ErrorResult errorResult = new ErrorResult("2", e.getMessage());
        return new ResponseEntity<>(errorResult, HttpStatus.BAD_GATEWAY);
    }
    @ResponseStatus(HttpStatus.TOO_EARLY)
    @ExceptionHandler // Exception e
    public ErrorResult three(Exception e) {
        return new ErrorResult("3", e.getMessage());
    }
    @GetMapping("/api2/{id}")
    public void run(@PathVariable("id") String id) {
        if (id.equals("one")) {
            throw new IllegalArgumentException("run-one");
        }
        if (id.equals("two")) {
            throw new UserException("run-two");
        }
        if (id.equals("three")) {
            throw new ResponseStatusException(HttpStatus.TOO_EARLY, "run-three");
        }
        if (id.equals("re")) {
            throw new RuntimeException("run-re");
        }
    }
}
