package hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Debug {

    @GetMapping("/debug")
    public String debug() {
        return "debug ok";
    }
}

