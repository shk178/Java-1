package hello.proxy.app.v3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService3 {
    private final OrderRepository3 orderRepository3;
    public void orderItem(String itemId) {
        orderRepository3.save(itemId);
    }
}
