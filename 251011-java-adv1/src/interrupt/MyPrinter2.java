package interrupt;

import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import static thread.MyLogger.log;

public class MyPrinter2 {
    public static void main(String[] args) {
        Printer2 prt = new Printer2();
        Thread t = new Thread(prt, "t-1");
        t.start();
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("입력 (종료 예약 q, 강제 종료 Q): ");
            String str = input.nextLine();
            if (str.equals("q")) {
                prt.work = false;
                break; //종료 예약 시 강제 종료 불가
            }
            if (str.equals("Q")) {
                t.interrupt(); //main도 멈춤
            }
            prt.addJob(str);
        }
    }
    static class Printer2 implements Runnable {
        volatile boolean work = true;
        Queue<String> jobQueue = new ConcurrentLinkedQueue<>();
        @Override
        public void run() {
            //run 메서드의 모든 시점에서 main이 입력을 받을 수 있다.
            try {
                Thread.sleep(5000); //시작 시간
                log("프린터 시작, 대기 문서: " + jobQueue);
            } catch (InterruptedException e) {
                log("프린터 강제 종료1");
                throw new RuntimeException(e);
            }
            while (work || !jobQueue.isEmpty()) {
                if (!work) {
                    log("프린터 종료 예약, 대기 문서: " + jobQueue);
                }
                if (jobQueue.isEmpty()) {
                    try {
                        Thread.sleep(5000); //CPU 과부하 방지
                    } catch (InterruptedException e) {
                        log("프린터 강제 종료2");
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                try {
                    String job = jobQueue.poll();
                    log("출력 시작: " + job + ", 대기 문서: " + jobQueue);
                    Thread.sleep(5000); //출력 시간
                    log("출력 완료: " + job + ", 대기 문서: " + jobQueue);
                } catch (InterruptedException e) {
                    log("프린터 강제 종료3");
                    throw new RuntimeException(e);
                }
            }
            try {
                Thread.sleep(5000); //종료 시간
                log("프린터 종료");
            } catch (InterruptedException e) {
                log("프린터 강제 종료4"); //불가
                throw new RuntimeException(e);
            }
        }
        public void addJob(String str) {
            jobQueue.offer(str);
        }
    }
}
/*
입력 (종료 예약 q, 강제 종료 Q): Q
입력 (종료 예약 q, 강제 종료 Q): 13:08:41.377 [      t-1] 프린터 강제 종료1
Exception in thread "t-1" java.lang.RuntimeException: java.lang.InterruptedException: sleep interrupted
	at interrupt.MyPrinter2$Printer2.run(MyPrinter2.java:38)
	at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.InterruptedException: sleep interrupted
	at java.base/java.lang.Thread.sleep0(Native Method)
	at java.base/java.lang.Thread.sleep(Thread.java:509)
	at interrupt.MyPrinter2$Printer2.run(MyPrinter2.java:34)
	... 1 more
 */
/*
입력 (종료 예약 q, 강제 종료 Q): 13:09:03.522 [      t-1] 프린터 시작, 대기 문서: []
Q
13:09:05.857 [      t-1] 프린터 강제 종료2
입력 (종료 예약 q, 강제 종료 Q): Exception in thread "t-1" java.lang.RuntimeException: java.lang.InterruptedException: sleep interrupted
	at interrupt.MyPrinter2$Printer2.run(MyPrinter2.java:49)
	at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.InterruptedException: sleep interrupted
	at java.base/java.lang.Thread.sleep0(Native Method)
	at java.base/java.lang.Thread.sleep(Thread.java:509)
	at interrupt.MyPrinter2$Printer2.run(MyPrinter2.java:46)
	... 1 more
 */
/*
입력 (종료 예약 q, 강제 종료 Q): 1
입력 (종료 예약 q, 강제 종료 Q): 13:09:40.236 [      t-1] 프린터 시작, 대기 문서: [1]
13:09:40.244 [      t-1] 출력 시작: 1, 대기 문서: []
Q
입력 (종료 예약 q, 강제 종료 Q): 13:09:42.981 [      t-1] 프린터 강제 종료3
Exception in thread "t-1" java.lang.RuntimeException: java.lang.InterruptedException: sleep interrupted
	at interrupt.MyPrinter2$Printer2.run(MyPrinter2.java:60)
	at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.InterruptedException: sleep interrupted
	at java.base/java.lang.Thread.sleep0(Native Method)
	at java.base/java.lang.Thread.sleep(Thread.java:509)
	at interrupt.MyPrinter2$Printer2.run(MyPrinter2.java:56)
	... 1 more
 */
/*
입력 (종료 예약 q, 강제 종료 Q): 1
입력 (종료 예약 q, 강제 종료 Q): 2
입력 (종료 예약 q, 강제 종료 Q): 13:10:44.191 [      t-1] 프린터 시작, 대기 문서: [1, 2]
13:10:44.211 [      t-1] 출력 시작: 1, 대기 문서: [2]
q
13:10:49.226 [      t-1] 출력 완료: 1, 대기 문서: [2]
13:10:49.226 [      t-1] 프린터 종료 예약, 대기 문서: [2]
13:10:49.226 [      t-1] 출력 시작: 2, 대기 문서: []
Q13:10:54.233 [      t-1] 출력 완료: 2, 대기 문서: []

13:10:59.237 [      t-1] 프린터 종료
 */