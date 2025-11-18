package lecture.demo2.web.controllers;

import lecture.demo2.domain.Member;
import lecture.demo2.domain.MemberRepository;
import lecture.demo2.web.ModelView;

import java.util.Map;

public class MemberSaveControllerV3 implements ControllerV3 {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));
        Member member = new Member(username, age);
        memberRepository.save(member);
        model.put("member", member); // 모델이 파라미터로 전달된다.
        return "save";
    }
}
