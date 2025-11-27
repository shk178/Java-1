package hello.thymeleaf.basic;

import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/basic")
public class BasicController4 {
    @Data
    public static class User {
        private String username;
    }
    @GetMapping("/javascript")
    public String javascript(Model model) {
        User userA = new User();
        userA.setUsername("A");
        model.addAttribute("user", userA);
        return "basic/javascript";
    }
    @GetMapping("/javascript2")
    public String javascript2(Model model) {
        List<User> userList = new ArrayList<>();
        User userA = new User();
        User userB = new User();
        userA.setUsername("A");
        userB.setUsername("B");
        userList.add(userA);
        userList.add(userB);
        model.addAttribute("userList", userList);
        return "basic/javascript2";
    }
}
