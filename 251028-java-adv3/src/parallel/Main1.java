package parallel;

import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Main1 {
    public static void main(String[] args) {
        // Runnable은 run() 메서드를 구현하며 void를 반환
        Runnable one = () -> HeavyJob.heavyTask(10);
        Runnable two = () -> HeavyJob.heavyTask(10, Thread.currentThread().getName());
        runnableRun(one);
        runnableRun(two);
        System.out.println();
        threadStart(one);
        threadStart(two);
        // Callable<T>는 call() 메서드를 구현하고 제네릭 타입 T의 값을 반환
        Callable<Integer> three = () -> {
            int sum = IntStream.rangeClosed(1, 3) // IntStream = [1, 2, 3]
                    .map(HeavyJob::heavyTask) // IntStream = [10, 20, 30]
                    .reduce(0, Integer::sum); // int = 0+10+20+30
            return sum;
        };
        /*
            List<Integer> list = IntStream.rangeClosed(1, 3) // IntStream = [1, 2, 3]
                    .map(HeavyJob::heavyTask) // 기본형 스트림 IntStream = [10, 20, 30]
                    .boxed() // 객체 스트림 Stream<Integer> = [10, 20, 30]
                    .collect(Collectors.toList()); // List<Integer> = [10, 20, 30]
         */
        Callable<Integer> four = () -> {
            int sum = IntStream.rangeClosed(1, 3)
                    .map(i -> HeavyJob.heavyTask(i, Thread.currentThread().getName()))
                    .reduce(0, Integer::sum);
            return sum;
        };
        System.out.println();
        callableCall(three);
        callableCall(four);
        System.out.println();
        futureGet(three);
        futureGet(four);
    }
    static void runnableRun(Runnable runnable) {
        long sTime = System.currentTimeMillis();
        runnable.run();
        long eTime = System.currentTimeMillis();
        MyLogger.log("duration=" + (eTime - sTime));
    }
    static void threadStart(Runnable runnable) {
        long sTime = System.currentTimeMillis();
        Thread t = new Thread(runnable);
        t.start();
        try {
            t.join(); // 스레드가 끝날 때까지 기다림
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long eTime = System.currentTimeMillis();
        MyLogger.log("duration=" + (eTime - sTime));
    }
    static void callableCall(Callable<?> callable) {
        long sTime = System.currentTimeMillis();
        Object result;
        try {
            result = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long eTime = System.currentTimeMillis();
        MyLogger.log("duration=" + (eTime - sTime) + ", result=" + result);
    }
    static <T> void futureGet(Callable<T> callable) {
        long sTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(callable);
        T result;
        try {
            result = future.get(); // 작업이 끝날 때까지 기다림
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();
        long eTime = System.currentTimeMillis();
        MyLogger.log("duration=" + (eTime - sTime) + ", result=" + result);
    }
}
/*
11:50:07.574 [     main] calculate 10 -> 100
11:50:08.584 [     main] duration=1032
11:50:08.588 [     main] [main] 10 -> 100
11:50:09.594 [     main] duration=1010

11:50:09.594 [ Thread-0] calculate 10 -> 100
11:50:10.602 [     main] duration=1008
11:50:10.602 [ Thread-1] [Thread-1] 10 -> 100
11:50:11.610 [     main] duration=1008

11:50:11.610 [     main] calculate 1 -> 10
11:50:12.618 [     main] calculate 2 -> 20
11:50:13.624 [     main] calculate 3 -> 30
11:50:14.636 [     main] duration=3022, result=60
11:50:14.636 [     main] [main] 1 -> 10
11:50:15.644 [     main] [main] 2 -> 20
11:50:16.651 [     main] [main] 3 -> 30
11:50:17.660 [     main] duration=3024, result=60

11:50:17.660 [pool-1-thread-1] calculate 1 -> 10
11:50:18.666 [pool-1-thread-1] calculate 2 -> 20
11:50:19.672 [pool-1-thread-1] calculate 3 -> 30
11:50:20.679 [     main] duration=3019, result=60
11:50:20.681 [pool-2-thread-1] [pool-2-thread-1] 1 -> 10
11:50:21.687 [pool-2-thread-1] [pool-2-thread-1] 2 -> 20
11:50:22.695 [pool-2-thread-1] [pool-2-thread-1] 3 -> 30
11:50:23.703 [     main] duration=3024, result=60
 */