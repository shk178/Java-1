package hello.advanced;

import hello.advanced.v4.ThreadLocalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ThreadLocalServiceTest {
    @Autowired
    private ThreadLocalService threadLocalService;
    @Test
    void field() throws InterruptedException {
        Runnable runA = () -> {
            threadLocalService.logic("userA");
        };
        Runnable runB = () -> {
            threadLocalService.logic("userB");
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
(저장 전) thName = th-B, name2 = userB, nameStore2.get() = null
(저장 전) thName = th-A, name2 = userA, nameStore2.get() = null
(저장 후) thName = th-B, name2 = userB, nameStore2.get() = userB
(저장 후) thName = th-A, name2 = userA, nameStore2.get() = userA
 */
/*
(저장 전) thName = th-B, name2 = userB, nameStore2.get() = null
(저장 전) thName = th-A, name2 = userA, nameStore2.get() = null
(저장 후) thName = th-A, name2 = userA, nameStore2.get() = userA
(저장 후) thName = th-B, name2 = userB, nameStore2.get() = userB
 */