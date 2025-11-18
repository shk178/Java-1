package lecture.demo2.web.controllers;

import lecture.demo2.domain.Member;
import lecture.demo2.domain.MemberRepository;
import lecture.demo2.web.ModelView;

import java.util.List;
import java.util.Map;

public class MemberListControllerV3 implements ControllerV3 {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        List<Member> members = memberRepository.findAll();
        model.put("members", members);
        return "list";
    }
}
