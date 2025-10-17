package executor;

import java.util.Random;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class RunnableMain {
    public static void main(String[] args) throws InterruptedException {
        MyRunnable task = new MyRunnable();
        Thread thread = new Thread(task);
        thread.start();
        thread.join();
        int result = task.value;
        log("result=" + result);
    }
    static class MyRunnable implements  Runnable {
        int value;
        @Override
        public void run() {
            log("Runnable 시작");
            sleep(2000);
            value = new Random().nextInt(10);
            log("create=" + value);
            log("Runnable 완료");
        }
    }
}
/*
19:36:07.816 [ Thread-0] Runnable 시작
19:36:09.824 [ Thread-0] create=3
19:36:09.824 [ Thread-0] Runnable 완료
19:36:09.824 [     main] result=3
 */