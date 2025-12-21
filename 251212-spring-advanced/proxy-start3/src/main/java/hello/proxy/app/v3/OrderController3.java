package hello.proxy.app.v3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RequiredArgsConstructor
@Controller
public class OrderController3 {
    private final OrderService3 orderService3;
    @GetMapping("/v3/req")
    public String req(String itemId) {
        log.info("OrderController3.req: itemId=" + itemId);
        orderService3.orderItem(itemId);
        return "ok";
    }
}
