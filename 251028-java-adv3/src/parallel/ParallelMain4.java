package parallel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class ParallelMain4 {
    public static void main(String[] args) {
        ExecutorService requestPool = Executors.newFixedThreadPool(10);
        ExecutorService logicPool = Executors.newFixedThreadPool(10);
        int nThreads = 3;
        for (int i = 1; i < nThreads; i++) {
            String requestName = "request-" + i;
            requestPool.submit(() -> logic(requestName, logicPool));
        }
        requestPool.close();
        logicPool.close();
    }
    private static void logic(String requestName, ExecutorService logicPool) {
        long sTime = System.currentTimeMillis();
        List<Future<Integer>> futures = IntStream.rangeClosed(1, 6)
                .mapToObj(i -> logicPool.submit(() -> HeavyJob.heavyTask(i, requestName)))
                .toList();
        int sum = futures.stream()
                .mapToInt(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).sum();
        long eTime = System.currentTimeMillis();
        MyLogger.log("(" + requestName + ") sum=" + sum + ", duration=" + (eTime - sTime));
    }
}
/*
16:04:24.341 [pool-2-thread-3] [request-2] 2 -> 20
16:04:24.341 [pool-2-thread-5] [request-2] 3 -> 30
16:04:24.341 [pool-2-thread-2] [request-2] 1 -> 10
16:04:24.341 [pool-2-thread-1] [request-1] 1 -> 10
16:04:24.341 [pool-2-thread-6] [request-1] 3 -> 30
16:04:24.341 [pool-2-thread-8] [request-1] 4 -> 40
16:04:24.341 [pool-2-thread-7] [request-2] 4 -> 40
16:04:24.341 [pool-2-thread-4] [request-1] 2 -> 20
16:04:24.341 [pool-2-thread-9] [request-2] 5 -> 50
16:04:24.341 [pool-2-thread-10] [request-1] 5 -> 50
16:04:25.352 [pool-2-thread-6] [request-1] 6 -> 60
16:04:25.352 [pool-2-thread-8] [request-2] 6 -> 60
16:04:26.364 [pool-1-thread-2] (request-2) sum=210, duration=2070
16:04:26.364 [pool-1-thread-1] (request-1) sum=210, duration=2070
 */