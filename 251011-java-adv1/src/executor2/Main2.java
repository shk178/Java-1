package executor2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static control.ThreadUtils.sleep;

public class Main2 {
    public static void main(String[] args) {
        ExecutorService es = new ThreadPoolExecutor(
                2, 4, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2)
        );
        es.execute(() -> sleep(1000)); //1. 스레드1 생성 → 실행
        es.execute(() -> sleep(1000)); //2. 스레드2 생성 → 실행
        es.execute(() -> sleep(1000)); //3. 큐[1] 대기
        es.execute(() -> sleep(1000)); //4. 큐[2] 대기 (큐 가득참)
        es.execute(() -> sleep(1000)); //5. 스레드3 생성 → 실행 (초과 스레드)
        es.execute(() -> sleep(1000)); //6. 스레드4 생성 → 실행 (초과 스레드)
        es.execute(() -> sleep(1000)); //7. 예외 발생
    }
}
//Exception in thread "main" java.util.concurrent.RejectedExecutionException: Task executor2.Main2$$Lambda/0x000001f509004440@30dae81 rejected from java.util.concurrent.ThreadPoolExecutor@34c45dca[Running, pool size = 4, active threads = 4, queued tasks = 2, completed tasks = 0]
//	at java.base/java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2081)
//	at java.base/java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:841)
//	at java.base/java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1376)
//	at executor2.Main2.main(Main2.java:22)