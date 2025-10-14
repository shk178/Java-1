package interrupt;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Main3 {
    //===== 로그 유틸 =====
    public static abstract class MyLogger {
        private static final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        public static void log(Object obj) {
            String t = LocalTime.now().format(f);
            System.out.printf("%s [%9s] %s\n", t, Thread.currentThread().getName(), obj);
        }
    }
    //===== sleep 유틸 =====
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //인터럽트 발생 시, 로그만 찍고 예외 던지지 않음
            MyLogger.log("인터럽트 발생: " + e.getMessage());
            //현재 스레드의 interrupted 상태를 다시 true로 만들어줌
            Thread.currentThread().interrupt();
        }
    }
    //===== Runnable 구현 클래스 =====
    static class MyTask implements Runnable {
        volatile boolean runFlag = true; //다른 스레드에서 변경 감지 가능
        @Override
        public void run() {
            while (runFlag && !Thread.currentThread().isInterrupted()) {
                MyLogger.log("작업 중");
                sleep(3000);
            }
            MyLogger.log("자원 정리, 종료");
        }
    }
    //===== main =====
    public static void main(String[] args) {
        MyTask m1 = new MyTask();
        Thread t1 = new Thread(m1, "thread-1");
        t1.start();
        sleep(4000);
        MyLogger.log("작업 중단 지시: interrupt()");
        t1.interrupt();
        MyLogger.log("t1.isInterrupted=" + t1.isInterrupted());
    }
}
/*
20:57:11.877 [ thread-1] 작업 중
20:57:14.882 [ thread-1] 작업 중
20:57:15.851 [     main] 작업 중단 지시: interrupt()
20:57:15.851 [ thread-1] 인터럽트 발생: sleep interrupted
20:57:15.851 [ thread-1] 자원 정리, 종료
20:57:15.854 [     main] t1.isInterrupted=true
 */