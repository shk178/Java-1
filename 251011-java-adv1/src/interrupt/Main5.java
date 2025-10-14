package interrupt;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class Main5 {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                log("작업1");
            }
            log("인터럽트 발생1");
            log("isInterrupted=" + Thread.currentThread().isInterrupted());
            try {
                log("작업2");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log("인터럽트 발생2");
                log("isInterrupted=" + Thread.currentThread().isInterrupted());
            }
            log("작업 종료");
        });
        t.start();
        sleep(20);
        t.interrupt();
        log("t.isInterrupted=" + t.isInterrupted());
    }
}
/*
11:23:47.151 [ Thread-0] 작업1
11:23:47.151 [ Thread-0] 작업1
...
11:23:47.151 [ Thread-0] 작업1
11:23:47.151 [ Thread-0] 작업1
11:23:47.153 [ Thread-0] 인터럽트 발생1
11:23:47.157 [     main] t.isInterrupted=true
11:23:47.157 [ Thread-0] isInterrupted=true
11:23:47.157 [ Thread-0] 작업2
11:23:47.157 [ Thread-0] 인터럽트 발생2
11:23:47.157 [ Thread-0] isInterrupted=false
11:23:47.158 [ Thread-0] 작업 종료
 */