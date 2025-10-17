package executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static control.ThreadUtils.sleep;
import static executor.ExecutorUtils.printState;

public class ExecutorBasicMain {
    public static void main(String[] args) {
        ExecutorService es = new ThreadPoolExecutor(2, 2, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        printState(es); //초기 상태
        es.execute(new RunnableTask("A"));
        es.execute(new RunnableTask("B"));
        es.execute(new RunnableTask("C"));
        es.execute(new RunnableTask("D"));
        printState(es); //작업 중
        sleep(3000);
        printState(es); //작업 완료
        es.close();
        printState(es); //shutdown 완료
    }
}
/*
19:19:24.424 [     main] [pool=0, active=0, queuedTasks=0, completedTasks=0] //초기 상태
19:19:24.436 [     main] [pool=2, active=2, queuedTasks=2, completedTasks=0] //작업 중
19:19:24.437 [pool-1-thread-1] A 시작
19:19:24.437 [pool-1-thread-2] B 시작
19:19:25.446 [pool-1-thread-1] A 종료
19:19:25.446 [pool-1-thread-2] B 종료
19:19:25.446 [pool-1-thread-1] C 시작
19:19:25.446 [pool-1-thread-2] D 시작
19:19:26.463 [pool-1-thread-1] C 종료
19:19:26.463 [pool-1-thread-2] D 종료
19:19:27.447 [     main] [pool=2, active=0, queuedTasks=0, completedTasks=4] //작업 완료
19:19:27.447 [     main] [pool=0, active=0, queuedTasks=0, completedTasks=4] //shutdown 완료
 */