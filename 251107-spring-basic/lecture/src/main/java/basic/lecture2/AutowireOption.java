package basic.lecture2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AutowireOption {
    @Autowired(required = false)
    public void setNoBean1(Member member) { // Member는 Bean이 아니라서 의존성 주입 안 됨
        System.out.println("setNoBean1 member = " + member); // 실행 안 됨
    }
    @Autowired
    public void setNoBean2(@Nullable Member member) {
        System.out.println("setNoBean2 member = " + member); // setNoBean2 member = null
    }
    @Autowired
    public void setNoBean3(Optional<Member> member) {
        System.out.println("setNoBean3 member = " + member); // setNoBean3 member = Optional.empty
    }
}
