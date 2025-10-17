package cas;

import java.util.concurrent.atomic.AtomicBoolean;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class CASLock {
    private final AtomicBoolean lock = new AtomicBoolean(false);
    public void lock() {
        while(true) {
            if (!lock.compareAndSet(false, true)) {
                log("락 획득x, 스핀 대기");
                sleep(100);
                //sleep 없음: busy-waiting, 락이 풀리는 순간 즉시 획득 가능 (응답성↑, CPU 낭비↑)
                //sleep 있음: 락이 풀려도 sleep에서 깨어날 때까지 지연 (응답성↓, CPU 절약↑)
                //락이 풀리는 순간은 sleep이 아니라 unlock에 달려 있다.
                //Thread.yield()나 LockSupport.parkNanos()가 더 효율적이다.
            } else {
                log("락 획득o");
                break;
            }
        }
    }
    public void unlock() {
        lock.set(false);
        log("락 반납");
    }
    public static void main(String[] args) {
        CASLock cas = new CASLock();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                cas.lock();
                try {
                    log("임계 영역");
                } finally {
                    cas.unlock();
                }
            }
        };
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(task);
            t.start();
        }
    }
}
/*
13:32:30.830 [ Thread-1] 락 획득x, 스핀 대기
13:32:30.830 [ Thread-2] 락 획득x, 스핀 대기
13:32:30.830 [ Thread-0] 락 획득o
13:32:30.833 [ Thread-0] 임계 영역
13:32:30.833 [ Thread-0] 락 반납
13:32:30.944 [ Thread-1] 락 획득x, 스핀 대기
13:32:30.944 [ Thread-2] 락 획득o
13:32:30.944 [ Thread-2] 임계 영역
13:32:30.944 [ Thread-2] 락 반납
13:32:31.058 [ Thread-1] 락 획득o
13:32:31.058 [ Thread-1] 임계 영역
13:32:31.058 [ Thread-1] 락 반납
 */