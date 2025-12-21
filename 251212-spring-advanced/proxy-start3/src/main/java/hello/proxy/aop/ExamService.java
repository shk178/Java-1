package hello.proxy.aop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    @ExamAnnotation
    @ExamAnnotation2
    public void req(String itemId) {
        examRepository.save(itemId);
    }
}
