package lecture.demo2.web.controllers;

import lecture.demo2.domain.Member;
import lecture.demo2.domain.MemberRepository;
import lecture.demo2.web.ModelView;

import java.util.Map;

public class MemberSaveControllerV2 implements ControllerV2 {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));
        Member member = new Member(username, age);
        memberRepository.save(member);
        ModelView modelView = new ModelView("save");
        modelView.getModel().put("member", member);
        return modelView;
    }
}
