package control;

import static thread.MyLogger.log;

public class JoinMain3 {
    public static void main(String[] args) {
        log("start");
        Sum s1 = new Sum(1, 50);
        Sum s2 = new Sum(51, 100);
        Thread t1 = new Thread(s1, "thread-1");
        Thread t2 = new Thread(s2, "thread-2");
        t1.start();
        t2.start();
        log("s1.sum=" + s1.getSum());
        log("s2.sum=" + s2.getSum());
        log("end");
    }
    static class Sum implements Runnable {
        private int startVal;
        private int endVal;
        private int sum;
        Sum(int x, int y) {
            log("초기화");
            startVal = x; endVal = y; sum = 0;
        }
        public int getSum() {
            return sum;
        }
        @Override
        public void run() {
            log("작업 시작");
            for (int i = startVal; i <= endVal; i++) {
                sum += i;
            }
            log("작업 완료: sum=" + sum);
        }
    }
}
/*
15:01:52.542 [     main] start
15:01:52.545 [     main] 초기화
15:01:52.545 [     main] 초기화
15:01:52.547 [ thread-1] 작업 시작
15:01:52.547 [ thread-2] 작업 시작
15:01:52.552 [ thread-2] 작업 완료: sum=3775
15:01:52.552 [     main] s1.sum=0
15:01:52.552 [ thread-1] 작업 완료: sum=1275
15:01:52.552 [     main] s2.sum=3775
15:01:52.552 [     main] end
 */