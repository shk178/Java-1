package parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class ParallelMain2 {
    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors());
        System.out.println(ForkJoinPool.commonPool().getParallelism());
        int sum = IntStream.rangeClosed(1, 10)
                .parallel() // 병렬 설정
                .map(HeavyJob::heavyTask)
                .reduce(0, Integer::sum);
        System.out.println(sum);
    }
}
/*
12
11
15:19:09.923 [     main] calculate 7 -> 70
15:19:09.923 [ForkJoinPool.commonPool-worker-6] calculate 4 -> 40
15:19:09.923 [ForkJoinPool.commonPool-worker-3] calculate 5 -> 50
15:19:09.923 [ForkJoinPool.commonPool-worker-7] calculate 10 -> 100
15:19:09.923 [ForkJoinPool.commonPool-worker-8] calculate 6 -> 60
15:19:09.923 [ForkJoinPool.commonPool-worker-1] calculate 3 -> 30
15:19:09.923 [ForkJoinPool.commonPool-worker-2] calculate 2 -> 20
15:19:09.923 [ForkJoinPool.commonPool-worker-4] calculate 9 -> 90
15:19:09.923 [ForkJoinPool.commonPool-worker-9] calculate 1 -> 10
15:19:09.923 [ForkJoinPool.commonPool-worker-5] calculate 8 -> 80
550
 */