package hello.proxy.app.v1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping
@ResponseBody
public interface OrderController1 {
    @GetMapping("/v1/req")
    String req(@RequestParam("itemId") String itemId);
    @GetMapping("/v1/req2")
    String req2();
}
