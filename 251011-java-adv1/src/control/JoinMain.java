package control;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class JoinMain {
    public static void main(String[] args) {
        log("main 시작");
        Thread t1 = new Thread(new Job(), "t-1");
        Thread t2 = new Thread(new Job(), "t-2");
        t1.start();
        t2.start();
        log("main 종료");
    }
    static class Job implements Runnable {
        @Override
        public void run() {
            log("작업 시작");
            sleep(2000);
            log("작업 종료");
        }
    }
}
/*
17:31:11.993 [     main] main 시작
17:31:12.004 [     main] main 종료
17:31:12.004 [      t-1] 작업 시작
17:31:12.004 [      t-2] 작업 시작
17:31:14.012 [      t-2] 작업 종료
17:31:14.012 [      t-1] 작업 종료
 */