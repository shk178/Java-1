package lecture.demo2.web.s6;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lecture.demo2.domain.Member;
import lecture.demo2.domain.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller; // Old Controller

import java.util.List;

@Component("/s6/old-controller")
public class OldController implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("OldController.handleRequest");
        MemberRepository repo = MemberRepository.getInstance();
        List<Member> members = repo.findAll();

        ModelAndView mv = new ModelAndView("list");
        mv.addObject("members", members);

        return mv;
    }
}
