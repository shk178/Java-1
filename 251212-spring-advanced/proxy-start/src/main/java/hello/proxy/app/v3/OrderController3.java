package hello.proxy.app.v3;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController3 {
    private final OrderService3 orderService3;
    @GetMapping("/v3/req")
    public String req(String itemId) {
        orderService3.orderItem(itemId);
        return "ok";
    }
    @GetMapping("/v3/req2")
    public String req2() {
        return "ok";
    }
}
