package volatile1;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class Main {
    public static void main(String[] args) {
        MyRunnable r = new MyRunnable();
        Thread t = new Thread(r);
        log("runFlag=" + r.runFlag);
        t.start();
        sleep(1000);
        r.runFlag = false;
        log("runFlag=" + r.runFlag);
    }
    static class MyRunnable implements Runnable {
        boolean runFlag = true;
        @Override
        public void run() {
            log("작업 시작");
            while(runFlag) {
            }
            log("작업 종료");
        }
    }
}
/*
14:58:53.766 [     main] runFlag=true
14:58:53.767 [ Thread-0] 작업 시작
14:58:54.781 [     main] runFlag=false
(main은 종료됨)
(while문이 종료가 안 됨)
 */