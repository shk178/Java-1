package hello.proxy.aop;

import org.springframework.stereotype.Repository;

@Repository
public class ExamRepository {
    private static int seq = 0;
    @ExamAnnotation
    @ExamAnnotation2
    public String save(String itemId) {
        seq++;
        if (seq % 5 == 0) {
            throw new RuntimeException("re");
        }
        return "ok";
    }
}
