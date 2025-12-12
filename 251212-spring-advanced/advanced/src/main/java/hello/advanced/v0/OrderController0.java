package hello.advanced.v0;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController0 {
    private final OrderService0 orderService0;
    @GetMapping("/v0/req")
    public String req(@RequestParam String itemId) {
        orderService0.orderItem(itemId);
        return "ok";
    }
}
