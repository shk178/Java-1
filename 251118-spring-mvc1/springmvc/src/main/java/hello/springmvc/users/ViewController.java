package hello.springmvc.users;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/users/view")
public class ViewController {
    @RequestMapping("/v1")
    public ModelAndView v1() {
        ModelAndView mav = new ModelAndView("hello-leaf").addObject("data", "hello!");
        return mav;
    }
    @RequestMapping("/v2")
    public String v2(Model model) {
        model.addAttribute("data", "hello!");
        return "hello-leaf";
    }
    @RequestMapping("/hello-leaf")
    /* 위치가 안 맞아서 실행 안 됨 */
    public void v3(Model model) {
        model.addAttribute("data", "hello!");
    }
}
