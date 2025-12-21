package hello.proxy.app.v3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService3 {
    private final OrderRepository3 orderRepository3;
    public void orderItem(String itemId) {
        log.info("OrderService3.orderItem: itemId=" + itemId);
        orderRepository3.save(itemId);
    }
}
