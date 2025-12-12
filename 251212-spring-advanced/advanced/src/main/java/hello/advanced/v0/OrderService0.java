package hello.advanced.v0;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService0 {
    private final OrderRepository0 orderRepository0;
    public void orderItem(String itemId) {
        orderRepository0.save(itemId);
    }
}
