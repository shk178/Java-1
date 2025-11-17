package basic.lecture2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConstructorLombok {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    // 컴파일 시점: .class에 생성자 자동으로 생성해준다.
    // build.gradle 내용 추가 및 적용
    // Lombok 플러그인 설치 + Enable annotation processing 설정
}
