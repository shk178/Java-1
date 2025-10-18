package executor2;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class RunnableTask implements Runnable {
    private final String name;
    private int sleepMs = 1000;
    public RunnableTask(String name) {
        this.name = name;
    }
    public RunnableTask(String name, int sleepMs) {
        this.name = name;
        this.sleepMs = sleepMs;
    }
    @Override
    public void run() {
        log(name + " 시작");
        try {
            Thread.sleep(sleepMs); //작업 시간 시뮬레이션
        } catch (InterruptedException e) {
            log("sleep 중 인터럽트 발생해서 잡음");
        }
        log(name + " 종료");
    }
}
