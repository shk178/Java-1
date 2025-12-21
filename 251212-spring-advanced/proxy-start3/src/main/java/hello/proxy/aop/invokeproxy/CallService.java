package hello.proxy.aop.invokeproxy;

import hello.proxy.aop.ExamAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 생성자 DI는 순환 사이클 만든다. 수정자 DI를 한다.
@Component
public class CallService {
    private CallService callService;
    @Autowired
    public void setCallService(CallService callService) {
        this.callService = callService;
    }
    @ExamAnnotation
    public void external() {
        System.out.println("CallService.external");
        callService.internal(1);
        this.internal(2);
        internal(3);
    }
    @ExamAnnotation
    public void internal(int i) {
        System.out.println("CallService.internal: " + i);
    }
}
