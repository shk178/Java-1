package parallel;

import java.util.concurrent.*;

public class Main3 {
    public static void main(String[] args) {
        SumTask task1 = new SumTask(1, 3);
        SumTask task2 = new SumTask(4, 6);
        SumTask task3 = new SumTask(1, 6);
        callablesCall(task1, task2);
        System.out.println();
        callablesCall(task3, null);
        System.out.println("---");
        futuresGet(task1, task2);
        System.out.println();
        futuresGet(task3, null);
    }
    static class SumTask implements Callable<Integer> {
        private int from;
        private int to;
        public SumTask(int from, int to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public Integer call() {
            int result = 0;
            for (int i = from; i <= to; i++) {
                result += HeavyJob.heavyTask(i);
            }
            return result;
        }
    }
    static void callablesCall(Callable callable1, Callable callable2) {
        long sTime = System.currentTimeMillis();
        Object result1 = null;
        Object result2 = null;
        try {
            result1 = callable1.call();
            result2 = callable2.call();
        } catch (NullPointerException e) {
            System.out.println("null");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long eTime = System.currentTimeMillis();
        MyLogger.log("duration=" + (eTime - sTime) + ", result1=" + result1 + ", result2=" + result2);
    }
    static <T> void futuresGet(Callable<T> callable1, Callable<T> callable2) {
        long sTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<T> future1 = null;
        Future<T> future2 = null;
        try {
            future1 = executor.submit(callable1);
            future2 = executor.submit(callable2);
        } catch (NullPointerException e) {
            System.out.println("null-1");
        }
        T result1 = null;
        T result2 = null;
        try {
            result1 = future1.get();
            result2 = future2.get();
        } catch (NullPointerException e) {
            System.out.println("null-2");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();
        long eTime = System.currentTimeMillis();
        MyLogger.log("duration=" + (eTime - sTime) + ", result1=" + result1 + ", result2=" + result2);
    }
}
/*
12:31:14.100 [     main] calculate 1 -> 10
12:31:15.115 [     main] calculate 2 -> 20
12:31:16.122 [     main] calculate 3 -> 30
12:31:17.133 [     main] calculate 4 -> 40
12:31:18.141 [     main] calculate 5 -> 50
12:31:19.153 [     main] calculate 6 -> 60
12:31:20.169 [     main] duration=6083, result1=60, result2=150

12:31:20.169 [     main] calculate 1 -> 10
12:31:21.172 [     main] calculate 2 -> 20
12:31:22.182 [     main] calculate 3 -> 30
12:31:23.190 [     main] calculate 4 -> 40
12:31:24.198 [     main] calculate 5 -> 50
12:31:25.207 [     main] calculate 6 -> 60
null
12:31:26.217 [     main] duration=6048, result1=210, result2=null
---
12:31:26.219 [pool-1-thread-2] calculate 4 -> 40
12:31:26.219 [pool-1-thread-1] calculate 1 -> 10
12:31:27.224 [pool-1-thread-2] calculate 5 -> 50
12:31:27.224 [pool-1-thread-1] calculate 2 -> 20
12:31:28.233 [pool-1-thread-2] calculate 6 -> 60
12:31:28.233 [pool-1-thread-1] calculate 3 -> 30
12:31:29.241 [     main] duration=3023, result1=60, result2=150

null-1
12:31:29.241 [pool-2-thread-1] calculate 1 -> 10
12:31:30.250 [pool-2-thread-1] calculate 2 -> 20
12:31:31.257 [pool-2-thread-1] calculate 3 -> 30
12:31:32.266 [pool-2-thread-1] calculate 4 -> 40
12:31:33.274 [pool-2-thread-1] calculate 5 -> 50
12:31:34.283 [pool-2-thread-1] calculate 6 -> 60
null-2
12:31:35.292 [     main] duration=6051, result1=210, result2=null
 */