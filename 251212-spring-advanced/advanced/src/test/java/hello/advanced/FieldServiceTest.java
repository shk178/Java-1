package hello.advanced;

import hello.advanced.v3.FieldService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FieldServiceTest {
    @Autowired
    private FieldService fieldService;
    @Test
    void field() throws InterruptedException {
        Runnable runA = () -> {
            fieldService.logic("userA");
        };
        Runnable runB = () -> {
            fieldService.logic("userB");
        };
        Thread thA = new Thread(runA, "th-A");
        Thread thB = new Thread(runB, "th-B");
        thA.start();
        thB.start();
        thB.join();
        thA.join();
    }
}
/*
(저장 전) thName = th-B, name = userB, nameStore = null
(저장 전) thName = th-A, name = userA, nameStore = null
(저장 후) thName = th-A, name = userA, nameStore = userA
(저장 후) thName = th-B, name = userB, nameStore = userA
 */
/*
(저장 전) thName = th-A, name = userA, nameStore = null
(저장 전) thName = th-B, name = userB, nameStore = null
(저장 후) thName = th-B, name = userB, nameStore = userB
(저장 후) thName = th-A, name = userA, nameStore = userB
 */