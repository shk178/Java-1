package executor;

import java.util.Random;
import java.util.concurrent.*;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class CallableMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(1);
        Future<Integer> future = es.submit(new MyCallable());
        Integer result = future.get();
        log("result=" + result);
        es.close();
    }
    static class MyCallable implements Callable<Integer> {
        @Override
        public Integer call() {
            log("Callable 시작");
            sleep(2000);
            int value = new Random().nextInt(10);
            log("create=" + value);
            log("Callable 완료");
            return value;
        }
    }
}
/*
19:44:42.921 [pool-1-thread-1] Callable 시작
19:44:44.936 [pool-1-thread-1] create=8
19:44:44.936 [pool-1-thread-1] Callable 완료
19:44:44.937 [     main] result=8
 */