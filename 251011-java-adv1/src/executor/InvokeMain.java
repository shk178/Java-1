package executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class InvokeMain {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService es = Executors.newFixedThreadPool(3);
        CallableTask task1 = new CallableTask("A", 1000);
        CallableTask task2 = new CallableTask("B", 2000);
        CallableTask task3 = new CallableTask("C", 3000);
        List<CallableTask> tasks1 = List.of(task1, task2, task3);
        List<Future<Integer>> futures1 = es.invokeAll(tasks1);
        for (Future<Integer> future : futures1) {
            log("result=" + future.get());
        }
        CallableTask task4 = new CallableTask("D", 4000);
        CallableTask task5 = new CallableTask("E", 5000);
        CallableTask task6 = new CallableTask("F", 6000);
        List<CallableTask> tasks2 = List.of(task4, task5, task6);
        List<Future<Integer>> futures2 = new ArrayList<>();
        for (Callable<Integer> task : tasks2) {
            futures2.add(es.submit(task));
        }
        log("result=" + es.invokeAny(tasks2)); //invokeAny는 Integer를 반환
        for (Future<Integer> future : futures2) {
            log("isCancelled=" + future.isCancelled());
        }
        es.close();
    }
    static class CallableTask implements Callable<Integer> {
        private String name;
        private int sleepMs;
        public CallableTask(String name, int sleepMs) {
            this.name = name;
            this.sleepMs = sleepMs;
        }
        @Override
        public Integer call() throws Exception {
            log(name + " 시작");
            sleep(sleepMs);
            log(name + " 완료, return=" + sleepMs);
            return sleepMs;
        }
    }
}
/*
21:41:22.682 [pool-1-thread-2] B 시작
21:41:22.682 [pool-1-thread-1] A 시작
21:41:22.682 [pool-1-thread-3] C 시작
21:41:23.704 [pool-1-thread-1] A 완료, return=1000
21:41:24.690 [pool-1-thread-2] B 완료, return=2000
21:41:25.697 [pool-1-thread-3] C 완료, return=3000
21:41:25.697 [     main] result=1000
21:41:25.697 [     main] result=2000
21:41:25.697 [     main] result=3000
21:41:25.700 [pool-1-thread-1] D 시작
21:41:25.700 [pool-1-thread-3] F 시작
21:41:25.700 [pool-1-thread-2] E 시작
21:41:29.710 [pool-1-thread-1] D 완료, return=4000
21:41:29.710 [pool-1-thread-1] D 시작
21:41:30.709 [pool-1-thread-2] E 완료, return=5000
21:41:30.709 [pool-1-thread-2] E 시작
21:41:31.708 [pool-1-thread-3] F 완료, return=6000
21:41:31.709 [pool-1-thread-3] F 시작
21:41:33.714 [pool-1-thread-1] D 완료, return=4000
//submit으로 받은 Future는
//invokeAny가 내부적으로 생성한 Future와 다른 객체다.
21:41:33.716 [     main] result=4000
21:41:33.716 [pool-1-thread-3] 인터럽트 발생: sleep interrupted
21:41:33.716 [pool-1-thread-2] 인터럽트 발생: sleep interrupted
//invokeAny하면 내부적으로 Future 만들어서 실행하고
//그 중 하나를 온료하고 나머지를 cancel(true)한다.
//submit으로 받은 Future에 cancel 호출이 전파되지 않는다.
21:41:33.721 [     main] isCancelled=false
21:41:33.721 [     main] isCancelled=false
21:41:33.721 [     main] isCancelled=false
//그래서 false다.
- ExecutorService는 풀에 있는 워커 스레드를 활용해 모든 작업을 병렬로 실행합니다.
- 가장 먼저 call()을 성공적으로 완료한 작업의 결과를 반환합니다.
- 나머지 작업은 cancel(true)로 인터럽트를 걸어 중단시킵니다.
 */