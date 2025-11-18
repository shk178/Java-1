package lecture.demo2.web.controllers;

import lecture.demo2.domain.Member;
import lecture.demo2.domain.MemberRepository;
import lecture.demo2.web.ModelView;

import java.util.List;
import java.util.Map;

public class MemberListControllerV2 implements ControllerV2 {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        List<Member> members = memberRepository.findAll();
        ModelView modelView = new ModelView("list");
        modelView.getModel().put("members", members);
        return modelView;
    }
}
