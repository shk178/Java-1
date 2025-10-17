package executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import static thread.MyLogger.log;

public class ExecutorUtils {
    public static void printState(ExecutorService executorService) {
        if (executorService instanceof ThreadPoolExecutor poolExecutor) {
            int pool = poolExecutor.getPoolSize(); //풀에서 관리되는 스레드 수
            int active = poolExecutor.getActiveCount(); //작업 수행 중 스레드 수
            int queuedTasks = poolExecutor.getQueue().size(); //큐에 대기 중 작업 수
            long completedTask = poolExecutor.getCompletedTaskCount(); //완료된 작업 수
            log("[pool=" + pool
            + ", active=" + active
            + ", queuedTasks=" + queuedTasks
            + ", completedTasks=" + completedTask
            + "]");
        } else {
            log(executorService);
        }
    }
}
