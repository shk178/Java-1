package interrupt;

import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class MyPrinter {
    public static void main(String[] args) {
        Printer prt = new Printer();
        Thread t = new Thread(prt, "t-1");
        t.start();
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("입력 (종료 q): ");
            String str = input.nextLine();
            if (str.equals("q")) {
                prt.work = false;
                break;
            }
            prt.addJob(str);
        }
    }
    static class Printer implements Runnable {
        volatile boolean work = true;
        Queue<String> jobQueue = new ConcurrentLinkedQueue<>();
        @Override
        public void run() {
            //run 메서드의 모든 시점에서 main이 입력을 받을 수 있다.
            sleep(5000); //시작 시간
            log("프린터 시작, 대기 문서: " + jobQueue);
            while (work || !jobQueue.isEmpty()) {
                if (!work) {
                    log("프린터 종료 예약, 대기 문서: " + jobQueue);
                }
                if (jobQueue.isEmpty()) {
                    sleep(1000); //CPU 과부하 방지
                    continue;
                }
                String job = jobQueue.poll();
                log("출력 시작: " + job + ", 대기 문서: " + jobQueue);
                sleep(5000); //출력 시간
                log("출력 완료: " + job + ", 대기 문서: " + jobQueue);
            }
            sleep(5000); //종료 시간
            log("프린터 종료");
        }
        public void addJob(String str) {
            jobQueue.offer(str);
        }
    }
}
/*
입력 (종료 q): 1
입력 (종료 q): 2
입력 (종료 q): 12:32:59.397 [      t-1] 프린터 시작, 대기 문서: [1, 2]
12:32:59.397 [      t-1] 출력 시작: 1, 대기 문서: [2]
12:33:04.410 [      t-1] 출력 완료: 1, 대기 문서: [2]
12:33:04.410 [      t-1] 출력 시작: 2, 대기 문서: []
3
입력 (종료 q): q
12:33:09.417 [      t-1] 출력 완료: 2, 대기 문서: [3]
12:33:09.417 [      t-1] 프린터 종료 예약, 대기 문서: [3]
12:33:09.417 [      t-1] 출력 시작: 3, 대기 문서: []
12:33:14.419 [      t-1] 출력 완료: 3, 대기 문서: []
12:33:19.428 [      t-1] 프린터 종료
 */