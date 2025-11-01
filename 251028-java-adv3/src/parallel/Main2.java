package parallel;

public class Main2 {
    public static void main(String[] args) {
        SumTask task1 = new SumTask(1, 3);
        SumTask task2 = new SumTask(4, 6);
        SumTask task3 = new SumTask(1, 6);
        runnableRun(task1);
        System.out.println();
        runnableRun(task2);
        System.out.println();
        runnableRun(task3);
        System.out.println("---");
        threadStart(task1);
        System.out.println();
        threadStart(task2);
        System.out.println();
        threadStart(task3);
    }
    static class SumTask implements Runnable {
        private int from;
        private int to;
        public int result;
        public SumTask(int from, int to) {
            this.from = from;
            this.to = to;
            result = 0;
        }
        @Override
        public void run() {
            for (int i = from; i <= to; i++) {
                result += HeavyJob.heavyTask(i);
            }
        }
    }
    static void runnableRun(SumTask runnable) {
        long sTime = System.currentTimeMillis();
        runnable.run();
        long eTime = System.currentTimeMillis();
        MyLogger.log("duration=" + (eTime - sTime) + ", result=" + runnable.result);
    }
    static void threadStart(SumTask runnable) {
        long sTime = System.currentTimeMillis();
        Thread t = new Thread(runnable);
        t.start();
        try {
            t.join(); // 스레드가 끝날 때까지 기다림
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long eTime = System.currentTimeMillis();
        MyLogger.log("duration=" + (eTime - sTime) + ", result=" + runnable.result);
    }
}
/*
12:09:46.706 [     main] calculate 1 -> 10
12:09:47.721 [     main] calculate 2 -> 20
12:09:48.736 [     main] calculate 3 -> 30
12:09:49.750 [     main] duration=3061, result=60

12:09:49.750 [     main] calculate 4 -> 40
12:09:50.752 [     main] calculate 5 -> 50
12:09:51.757 [     main] calculate 6 -> 60
12:09:52.767 [     main] duration=3017, result=150

12:09:52.767 [     main] calculate 1 -> 10
12:09:53.778 [     main] calculate 2 -> 20
12:09:54.787 [     main] calculate 3 -> 30
12:09:55.796 [     main] calculate 4 -> 40
12:09:56.803 [     main] calculate 5 -> 50
12:09:57.809 [     main] calculate 6 -> 60
12:09:58.819 [     main] duration=6052, result=210
---
12:09:58.819 [ Thread-0] calculate 1 -> 10
12:09:59.827 [ Thread-0] calculate 2 -> 20
12:10:00.831 [ Thread-0] calculate 3 -> 30
12:10:01.841 [     main] duration=3022, result=120

12:10:01.841 [ Thread-1] calculate 4 -> 40
12:10:02.848 [ Thread-1] calculate 5 -> 50
12:10:03.853 [ Thread-1] calculate 6 -> 60
12:10:04.867 [     main] duration=3026, result=300

12:10:04.867 [ Thread-2] calculate 1 -> 10
12:10:05.875 [ Thread-2] calculate 2 -> 20
12:10:06.883 [ Thread-2] calculate 3 -> 30
12:10:07.891 [ Thread-2] calculate 4 -> 40
12:10:08.896 [ Thread-2] calculate 5 -> 50
12:10:09.902 [ Thread-2] calculate 6 -> 60
12:10:10.909 [     main] duration=6042, result=420
 */