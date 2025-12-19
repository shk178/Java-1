package hello.proxy.app.v3;

import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository3 {
    public void save(String itemId) {
        if (itemId.equals("re")) {
            throw new RuntimeException("re");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
