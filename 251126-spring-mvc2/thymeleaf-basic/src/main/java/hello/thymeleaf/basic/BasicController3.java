package hello.thymeleaf.basic;

import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping("/basic")
public class BasicController3 {
    @GetMapping("/attribute")
    public String attribute() {
        return "basic/attribute";
    }
    @Data
    static class User {
        private String username;
    }
    private void addUsers(Model model) {
        List<User> userList = new ArrayList<>();
        User userA = new User();
        User userB = new User();
        userA.setUsername("A");
        userB.setUsername("B");
        userList.add(userA);
        userList.add(userB);
        model.addAttribute(userList);
    }
    @GetMapping("/each")
    public String each(Model model) {
        addUsers(model);
        return "basic/each";
        /*
        username
        A
        B
        count	username	etc
        1	A	0 1 2 false true true false BasicController3.User(username=A)
        2	B	1 2 2 true false false true BasicController3.User(username=B)
         */
    }
    @GetMapping("/condition")
    public String condition(Model model) {
        addUsers(model);
        return "basic/condition";
        /*
        username
        A
        B
        count	username	if, unless	switch
        1	A	count가 1이다.	username이 A다.
        2	B	count가 1이 아니다.	username이 A가 아니다.
         */
    }
    @GetMapping("/comments")
    public String comments(Model model) {
        model.addAttribute("data", "spring");
        return "basic/comments";
        // html data /*spring*/
    }
    @GetMapping("/block")
    public String block(Model model) {
        addUsers(model);
        return "basic/block";
        /*
        1 A
        2 B
         */
    }
}
