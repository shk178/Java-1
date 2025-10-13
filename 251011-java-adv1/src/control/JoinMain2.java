package control;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class JoinMain2 {
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
            sleep(2000);
            for (int i = startVal; i <= endVal; i++) {
                sum += i;
            }
            log("작업 완료: sum=" + sum);
        }
    }
}
/*
14:59:51.232 [     main] start
14:59:51.232 [     main] 초기화
14:59:51.232 [     main] 초기화
14:59:51.236 [ thread-1] 작업 시작
14:59:51.236 [ thread-2] 작업 시작
14:59:51.239 [     main] s1.sum=0
14:59:51.241 [     main] s2.sum=0
14:59:51.241 [     main] end
14:59:53.248 [ thread-1] 작업 완료: sum=1275
14:59:53.248 [ thread-2] 작업 완료: sum=3775
 */