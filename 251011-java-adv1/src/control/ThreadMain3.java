package control;
import static thread.MyLogger.log;

public class ThreadMain3 {
    public static void main(String[] args) {
        Thread maint = Thread.currentThread();
        Thread t = new Thread(new MyRunnable(maint), "myThread");
        log(maint.getState());
        log(t.getState());
        t.start();
        log("run start()");
    }
    //main 스레드는 main() 메서드 안에서만 지역 변수로 선언되어 있다.
    static class MyRunnable implements Runnable {
        private final Thread maint;
        MyRunnable(Thread maint) {
            this.maint = maint;
        }
        @Override
        public void run() {
            log(maint.getState());
            log(Thread.currentThread().getState());
            log("run run()");
        }
    }
}
/*
16:45:34.811 [     main] RUNNABLE
16:45:34.813 [     main] NEW
16:45:34.813 [     main] run start()
16:45:34.813 [ myThread] RUNNABLE
16:45:34.813 [ myThread] RUNNABLE
16:45:34.813 [ myThread] run run()
 */