package executor;

import java.util.Random;
import java.util.concurrent.*;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class CallableMain2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(1);
        Future<Integer> future = es.submit(new MyCallable());
        log("es.submit() 완료, future=" + future);
        log("future.get() 시작, 완료까지 main=WAITING");
        Integer result = future.get();
        log("future.get() 완료, result=" + result);
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
19:59:47.793 [pool-1-thread-1] Callable 시작
19:59:47.793 [     main] es.submit() 완료, future=java.util.concurrent.FutureTask@4fca772d[Not completed, task = executor.CallableMain2$MyCallable@506e1b77]
19:59:47.796 [     main] future.get() 시작, 완료까지 main=WAITING
19:59:49.814 [pool-1-thread-1] create=8
19:59:49.814 [pool-1-thread-1] Callable 완료
19:59:49.816 [     main] future.get() 완료, result=8
 */