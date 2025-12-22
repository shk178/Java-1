package hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MemoryTest {
    @Autowired
    MemoryController memoryController;
    @Test
    void test() {
        memoryController.system();
    }
}
/*
컨트롤러 실행됨
Memory{used=29520848, max=536870912}
 */