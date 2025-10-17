package executor.ex;

import executor.InvokeMain;

import java.util.List;
import java.util.concurrent.*;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

/*
커머스 회사의 주문 팀
연동 시스템 많아지면서 주문 프로세스 오래 걸린다.
하나의 주문 3가지 업무
- 재고 업데이트 1초, 배송 신청 1초, 회계 업데이트 1초
고객은 3초 대기해야 한다.
3가지 호출 순서는 상관 없고 주문 번호만 잘 전달하면 된다.
3가지 업무 모두 성공해야 주문 완료된다.
 */
public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        order("1000");
        order("1001");
    }
    static void order(String orderNo) throws InterruptedException, ExecutionException {
        InventoryWork inventoryWork = new InventoryWork(orderNo);
        ShippingWork shippingWork = new ShippingWork(orderNo);
        AccountingWork accountingWork = new AccountingWork(orderNo);
        ExecutorService es = Executors.newFixedThreadPool(3);
        List<Callable<Boolean>> tasks = List.of(inventoryWork, shippingWork, accountingWork);
        List<Future<Boolean>> futures = es.invokeAll(tasks);
        Boolean result = true;
        for (Future<Boolean> future : futures) {
            result &= future.get();
        }
        if (result) {
            log("완료");
        }
        es.close();
    }
    static class InventoryWork implements Callable<Boolean> {
        private final String orderNo;
        public InventoryWork(String orderNo) {
            this.orderNo = orderNo;
        }
        @Override
        public Boolean call() {
            log(getClass() + ": orderNo=" + orderNo);
            sleep(1000);
            return true;
        }
    }
    static class ShippingWork implements Callable<Boolean> {
        private final String orderNo;
        public ShippingWork(String orderNo) {
            this.orderNo = orderNo;
        }
        @Override
        public Boolean call() {
            log(getClass() + ": orderNo=" + orderNo);
            sleep(1000);
            return true;
        }
    }
    static class AccountingWork implements Callable<Boolean> {
        private final String orderNo;
        public AccountingWork(String orderNo) {
            this.orderNo = orderNo;
        }
        @Override
        public Boolean call() {
            log(getClass() + ": orderNo=" + orderNo);
            sleep(1000);
            return true;
        }
    }
}
/*
22:07:53.757 [pool-1-thread-2] class executor.ex.Main$ShippingWork: orderNo=1000
22:07:53.757 [pool-1-thread-1] class executor.ex.Main$InventoryWork: orderNo=1000
22:07:53.757 [pool-1-thread-3] class executor.ex.Main$AccountingWork: orderNo=1000
22:07:54.766 [     main] 완료
22:07:54.766 [pool-2-thread-1] class executor.ex.Main$InventoryWork: orderNo=1001
22:07:54.766 [pool-2-thread-2] class executor.ex.Main$ShippingWork: orderNo=1001
22:07:54.766 [pool-2-thread-3] class executor.ex.Main$AccountingWork: orderNo=1001
22:07:55.770 [     main] 완료
 */