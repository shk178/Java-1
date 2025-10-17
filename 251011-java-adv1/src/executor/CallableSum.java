package executor;

import java.util.concurrent.*;

import static thread.MyLogger.log;

public class CallableSum {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        SumTask task1 = new SumTask(1, 50);
        SumTask task2 = new SumTask(51, 100);
        ExecutorService es = Executors.newFixedThreadPool(2);
        Future<Integer> future1 = es.submit(task1);
        Future<Integer> future2 = es.submit(task2);
        Integer sum1 = future1.get(); //main이 2초 간 대기
        Integer sum2 = future2.get(); //이미 완료돼서 블로킹x
        log("task1.result=" + sum1);
        log("task2.result=" + sum2);
        log("total sum=" + (sum1+ sum2));
        es.close();
    }
    static class SumTask implements Callable<Integer> {
        int startValue;
        int endValue;
        public SumTask(int startValue, int endValue) {
            this.startValue = startValue;
            this.endValue = endValue;
        }
        @Override
        public Integer call() throws InterruptedException {
            log("작업 시작");
            Thread.sleep(2000);
            int sum = 0;
            for (int i = startValue; i <= endValue; i++) {
                sum += i;
            }
            log("작업 완료 sum=" + sum);
            return sum;
        }
    }
}
/*
20:32:12.364 [pool-1-thread-1] 작업 시작
20:32:12.364 [pool-1-thread-2] 작업 시작
20:32:14.385 [pool-1-thread-2] 작업 완료 sum=3775
20:32:14.385 [pool-1-thread-1] 작업 완료 sum=1275
20:32:14.385 [     main] task1.result=1275
20:32:14.385 [     main] task2.result=3775
20:32:14.385 [     main] total sum=5050
 */