package interrupt;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class Main2 {
    public static void main(String[] args) {
        MyTask m1 = new MyTask();
        Thread t1 = new Thread(m1, "thread-1");
        t1.start();
        sleep(4000);
        log("작업 중단 지시: interrupt()");
        t1.interrupt();
        log("t1.isInterrupted=" + t1.isInterrupted());
    }
    static class MyTask implements Runnable {
        volatile boolean runFlag = true;
        @Override
        public void run() {
            while (runFlag) {
                log("작업 중");
                sleep(3000);
            }
            log("자원 정리, 종료");
        }
    }
}
/*
20:23:58.848 [ thread-1] 작업 중
20:24:01.861 [ thread-1] 작업 중
20:24:02.841 [     main] 작업 중단 지시: interrupt()
20:24:02.845 [     main] t1.isInterrupted=true
20:24:02.853 [ thread-1] 인터럽트 발생: sleep interrupted
Exception in thread "thread-1" java.lang.RuntimeException: java.lang.InterruptedException: sleep interrupted
	at control.ThreadUtils.sleep(ThreadUtils.java:11)
	at interrupt.Main2$MyTask.run(Main2.java:22)
	at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.InterruptedException: sleep interrupted
	at java.base/java.lang.Thread.sleep0(Native Method)
	at java.base/java.lang.Thread.sleep(Thread.java:509)
	at control.ThreadUtils.sleep(ThreadUtils.java:8)
	... 2 more
 */