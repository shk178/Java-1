package spring.tx;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    @Transactional
    public void order(Order order) throws BzException {
        orderRepository.save(order);
        String exceptCase = order.getUsername();
        if (exceptCase.equals("sys")) {
            throw new RuntimeException("시스템 예외");
        } else if (exceptCase.equals("bz")) {
            order.setPayStatus("대기");
            throw new BzException("비즈니스 예외");
        } else {
            order.setPayStatus("완료");
        }
    }
}
