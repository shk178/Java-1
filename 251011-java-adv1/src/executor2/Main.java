package executor2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static executor.ExecutorUtils.printState;
import static thread.MyLogger.log;

public class Main {
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(2); //워커 스레드 2개
        es.execute(new RunnableTask("A")); //태스크 A → 즉시 실행 (스레드1)
        es.execute(new RunnableTask("B")); //태스크 B → 즉시 실행 (스레드2)
        es.execute(new RunnableTask("C")); //태스크 C → 큐 대기
        es.execute(new RunnableTask("D", 100_000)); //태스크 D (100초 소요) → 큐 대기
        printState(es); //상태: RUNNING, isShutdown=false, isTerminated=false
        shutdownAndAwaitTermination(es); //우아한 종료 시도
        printState(es); //상태: TERMINATED (또는 SHUTDOWN)
        es.close(); //이미 종료되었으면 아무 일도 안 함
    }
    private static void shutdownAndAwaitTermination(ExecutorService es) {
        //새로운 작업 제출을 거부 - execute(), submit() 호출 시 RejectedExecutionException 발생
        //이미 큐에 들어간 작업들은 모두 끝까지 수행 - 즉시 스레드를 중단하지는 않는다.
        //상태는 RUNNING → SHUTDOWN으로 바뀜
        es.shutdown();
        try {
            //모든 작업이 끝나고 스레드풀 종료될 때까지 최대 10초간 기다림
            //true: 모든 작업이 10초 내에 끝남 → 정상 종료
            //false: 아직 안 끝남 → 다음 단계로
            log("첫 번째 await 시작");
            if (!es.awaitTermination(10, TimeUnit.SECONDS)) {
                log("shutdown 타임아웃");
                //강제 종료 시도
                //실행 중이던 스레드에 interrupt()를 걸어 중단 요청
                //큐에 남은 대기 작업들을 모두 꺼내서 반환
                //대부분의 Runnable / Callable이 InterruptedException 감지 시 종료하도록 작성됨
                es.shutdownNow();
                //강제 종료 명령 후 다시 한 번 10초 기다림
                log("두 번째 await 시작");
                if(!es.awaitTermination(10, TimeUnit.SECONDS)) {
                    //그래도 종료가 안 되면, 로그 남기는 등만 하고 다음으로 넘어감
                    log("shutdownNow 타임아웃");
                } else {
                    log("shutdownNow 완료");
                }
            }
        } catch (InterruptedException e) { //만약 await 대기 중 현재 스레드가 인터럽트되면 예외 발생
            es.shutdownNow(); //보통 shutdownNow()를 다시 호출한 후
            Thread.currentThread().interrupt(); //스레드의 인터럽트 상태를 복원
        } //현재 스레드의 예외만 잡을 수 있다. 다른 스레드의 예외는 못 잡는다.
    }
}
/*
14:05:20.840 [     main] [pool=2, active=2, queuedTasks=2, completedTasks=0]
14:05:20.840 [pool-1-thread-1] A 시작
14:05:20.840 [pool-1-thread-2] B 시작
14:05:20.843 [     main] 첫 번째 await 시작
14:05:21.856 [pool-1-thread-1] A 종료
14:05:21.856 [pool-1-thread-2] B 종료
14:05:21.856 [pool-1-thread-1] C 시작
14:05:21.856 [pool-1-thread-2] D 시작
14:05:22.859 [pool-1-thread-1] C 종료
14:05:30.844 [     main] shutdown 타임아웃
14:05:30.844 [     main] 두 번째 await 시작
14:05:30.844 [pool-1-thread-2] sleep 중 인터럽트 발생해서 잡음
14:05:30.844 [pool-1-thread-2] D 종료
14:05:30.844 [     main] shutdownNow 완료
14:05:30.844 [     main] [pool=0, active=0, queuedTasks=0, completedTasks=4]
 */