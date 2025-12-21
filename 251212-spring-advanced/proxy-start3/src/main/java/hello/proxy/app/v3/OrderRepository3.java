package hello.proxy.app.v3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class OrderRepository3 {
    public void save(String itemId) {
        log.info("OrderRepository3.save: itemId=" + itemId);
        if (itemId.equals("re")) {
            throw new RuntimeException("re");
        }
    }
}
