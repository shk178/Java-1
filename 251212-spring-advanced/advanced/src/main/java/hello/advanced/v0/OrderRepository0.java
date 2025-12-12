package hello.advanced.v0;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository0 {
    public void save(String itemId) {
        if (itemId.equals("re")) {
            throw new RuntimeException("re");
        }
        sleep(1000);
    }
    private void sleep(int millies) {
        try {
            Thread.sleep(millies);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
