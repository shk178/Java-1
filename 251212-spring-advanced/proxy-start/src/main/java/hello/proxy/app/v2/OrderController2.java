package hello.proxy.app.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping
@ResponseBody
@RequiredArgsConstructor
public class OrderController2 {
    private final OrderService2 orderService2;
    @GetMapping("/v2/req")
    public String req(String itemId) {
        orderService2.orderItem(itemId);
        return "ok";
    }
    @GetMapping("/v2/req2")
    public String req2() {
        return "ok";
    }
}
