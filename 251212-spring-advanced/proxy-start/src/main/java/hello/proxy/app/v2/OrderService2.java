package hello.proxy.app.v2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderService2 {
    private final OrderRepository2 orderRepository2;
    public void orderItem(String itemId) {
        orderRepository2.save(itemId);
    }
}
