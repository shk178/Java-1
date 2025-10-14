package interrupt;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class Main {
    public static void main(String[] args) {
        MyTask m1 = new MyTask();
        Thread t1 = new Thread(m1, "thread-1");
        t1.start();
        sleep(4000);
        log("작업 중단 지시: runFlag=false");
        m1.runFlag = false;
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
20:15:15.417 [ thread-1] 작업 중
20:15:18.420 [ thread-1] 작업 중
20:15:19.403 [     main] 작업 중단 지시: runFlag=false
20:15:21.421 [ thread-1] 자원 정리, 종료
 */