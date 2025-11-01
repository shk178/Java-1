package parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ParallelMain3 {
    public static void main(String[] args) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "3");
        ExecutorService requestPool = Executors.newFixedThreadPool(100);
        int nThreads = 3;
        for (int i = 1; i < nThreads; i++) {
            String requestName = "request-" + i;
            requestPool.submit(() -> logic(requestName));
        }
        requestPool.close();
    }
    private static void logic(String requestName) {
        long sTime = System.currentTimeMillis();
        int sum = IntStream.rangeClosed(1, 6)
                .parallel()
                .map(i -> HeavyJob.heavyTask(i, requestName))
                .reduce(0, Integer::sum);
        long eTime = System.currentTimeMillis();
        MyLogger.log("(" + requestName + ") sum=" + sum + ", duration=" + (eTime - sTime));
    }
}
/*
15:46:54.775 [pool-1-thread-1] [request-1] 4 -> 40
15:46:54.775 [ForkJoinPool.commonPool-worker-2] [request-2] 2 -> 20
15:46:54.775 [ForkJoinPool.commonPool-worker-1] [request-1] 2 -> 20
15:46:54.775 [pool-1-thread-2] [request-2] 4 -> 40
15:46:54.775 [ForkJoinPool.commonPool-worker-3] [request-1] 6 -> 60
15:46:55.778 [pool-1-thread-2] [request-2] 6 -> 60
15:46:55.778 [ForkJoinPool.commonPool-worker-2] [request-2] 3 -> 30
15:46:55.778 [ForkJoinPool.commonPool-worker-1] [request-1] 3 -> 30
15:46:55.778 [pool-1-thread-1] [request-1] 1 -> 10
15:46:55.778 [ForkJoinPool.commonPool-worker-3] [request-1] 5 -> 50
15:46:56.786 [pool-1-thread-2] [request-2] 5 -> 50
15:46:56.786 [ForkJoinPool.commonPool-worker-2] [request-2] 1 -> 10
15:46:56.791 [pool-1-thread-1] (request-1) sum=210, duration=2045
15:46:57.792 [pool-1-thread-2] (request-2) sum=210, duration=3051
 */