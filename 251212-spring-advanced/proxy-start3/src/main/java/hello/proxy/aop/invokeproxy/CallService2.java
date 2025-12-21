package hello.proxy.aop.invokeproxy;

import hello.proxy.aop.ExamAnnotation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

// 지연 조회
@Component
@RequiredArgsConstructor
public class CallService2 {
    private final ObjectProvider<CallService2> provider;
    @ExamAnnotation
    public void external() {
        System.out.println("CallService2.external");
        CallService2 callService2 = provider.getObject();
        callService2.internal(1);
        this.internal(2);
        internal(3);
    }
    @ExamAnnotation
    public void internal(int i) {
        System.out.println("CallService2.internal: " + i);
    }
}
