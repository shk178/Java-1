package executor;

import java.util.concurrent.*;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class FutureCancel {
    private static boolean mayInterruptIfRunning = true;
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(1);
        Future<String> future = es.submit(new MyTask());
        log("es.submit: future.state=" + future.state());
        sleep(3000);
        boolean result = future.cancel(mayInterruptIfRunning);
        log("future.cancel: future.state=" + future.state());
        try {
            log("future.get=" + future.get());
        } catch (CancellationException e) {
            log("Future는 이미 취소 되었습니다.");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        es.close();
    }
    static class MyTask implements Callable<String> {
        @Override
        public String call() {
            for (int i = 0; i < 10; i++) {
                log("작업 중: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return "Interrupted";
                }
            }
            return "Completed";
        }
    }
}
/*
20:56:15.408 [     main] es.submit: future.state=RUNNING
20:56:15.408 [pool-1-thread-1] 작업 중: 0
20:56:16.414 [pool-1-thread-1] 작업 중: 1
20:56:17.421 [pool-1-thread-1] 작업 중: 2
20:56:18.426 [pool-1-thread-1] 작업 중: 3
20:56:18.426 [     main] future.cancel: future.state=CANCELLED
20:56:18.427 [     main] Future는 이미 취소 되었습니다.
 */