package hello.proxy.app.v1;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderService1Impl implements OrderService1 {
    private final OrderRepository1 orderRepository1;
    @Override
    public void orderItem(String itemId) {
        orderRepository1.save(itemId);
    }
}
