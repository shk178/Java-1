package hello.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @GetMapping("/order")
    public String order() {
        orderService.order();
        return "ok";
    }
    @GetMapping("/cancel")
    public String cancel() {
        orderService.cancleOrder();
        return "ok";
    }
}
