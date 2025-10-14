package volatile1;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class Count {
    public static void main(String[] args) {
        Counter c = new Counter();
        Thread t = new Thread(c);
        t.start();
        sleep(1000);
        c.flag = false;
        log("flag="+ c.flag + ", count=" + c.count);
    }
    static class Counter implements Runnable {
        volatile boolean flag = true;
        long count;
        @Override
        public void run() {
            while(flag) {
                count++;
                if (count % 100_000_000 == 0) {
                    log("flag="+ flag + ", count=" + count);
                }
            }
            log("flag="+ flag + ", count=" + count);
        }
    }
}
//15:49:20.923 [ Thread-0] flag=true, count=100000000
//15:49:21.066 [ Thread-0] flag=true, count=200000000
//15:49:21.194 [ Thread-0] flag=true, count=300000000
//15:49:21.321 [ Thread-0] flag=true, count=400000000
//15:49:21.448 [ Thread-0] flag=true, count=500000000
//15:49:21.574 [ Thread-0] flag=true, count=600000000
//15:49:21.706 [ Thread-0] flag=true, count=700000000
//15:49:21.748 [ Thread-0] flag=false, count=728488982
//15:49:21.748 [     main] flag=false, count=728488982
