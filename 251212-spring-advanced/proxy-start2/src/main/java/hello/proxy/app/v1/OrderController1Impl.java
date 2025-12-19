package hello.proxy.app.v1;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderController1Impl implements OrderController1 {
    private final OrderService1 orderService1;

    @Override
    public String req(String itemId) {
        orderService1.orderItem(itemId);
        return "ok";
    }

    @Override
    public String req2() {
        return "ok";
    }
}
