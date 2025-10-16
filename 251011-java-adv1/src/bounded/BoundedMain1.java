package bounded;

import java.util.ArrayList;
import java.util.List;
import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class BoundedMain1 {
    public static void main(String[] args) {
        //1. BoundedQueue 선택
        BoundedQueue queue1 = new BoundedQueue1(2);
        BoundedQueue queue2 = new BoundedQueue1(2);
        //2. 생산자, 소비자 실행 순서 선택: 반드시 하나만 선택
        producerFirst(queue1); //생산자 먼저 실행
        consumerFirst(queue2); //소비자 먼저 실행
    }
    private static void producerFirst(BoundedQueue queue) {
        log("[producerFirst] queueName=" + queue.getClass().getSimpleName());
        List<Thread> threads = new ArrayList<>();
        startProducer(queue, threads);
        printAllState(queue, threads);
        startConsumer(queue, threads);
        printAllState(queue, threads);
    }
    private static void consumerFirst(BoundedQueue queue) {
        log("[consumerFirst] queueName=" + queue.getClass().getSimpleName());
        List<Thread> threads = new ArrayList<>();
        startConsumer(queue, threads);
        printAllState(queue, threads);
        startProducer(queue, threads);
        printAllState(queue, threads);
    }
    private static void startProducer(BoundedQueue queue, List<Thread> threads) {
        log("[startProducer]");
        for (int i = 1; i < 4; i++) {
            Thread producer = new Thread(new ProducerTask(queue, "data" + i), "생산자" + i);
            threads.add(producer);
            producer.start();
            sleep(100);
        }
    }
    private static void startConsumer(BoundedQueue queue, List<Thread> threads) {
        log("[startConsumer]");
        for (int i = 1; i < 4; i++) {
            Thread consumer = new Thread(new ConsumerTask(queue), "소비자" + i);
            threads.add(consumer);
            consumer.start();
            sleep(100);
        }
    }
    private static void printAllState(BoundedQueue queue, List<Thread> threads) {
        log("[printAllState] queue=" + queue);
        for (Thread thread : threads) {
            log("[printAllState] threadState=" + thread.getState());
        }
    }
}
/*
16:59:57.357 [     main] [producerFirst] queueName=BoundedQueue1
16:59:57.359 [     main] [startProducer]
16:59:57.370 [     생산자1] [run-생산 시도] data1 -> []
16:59:57.371 [     생산자1] [run-생산 완료] data1 in [data1]
16:59:57.469 [     생산자2] [run-생산 시도] data2 -> [data1]
16:59:57.469 [     생산자2] [run-생산 완료] data2 in [data1, data2]
16:59:57.587 [     생산자3] [run-생산 시도] data3 -> [data1, data2]
16:59:57.588 [     생산자3] [put] 큐가 가득 참, 버림: data3
16:59:57.588 [     생산자3] [run-생산 완료] data3 in [data1, data2]
16:59:57.700 [     main] [printAllState] queue=[data1, data2]
16:59:57.700 [     main] [printAllState] threadState=TERMINATED
16:59:57.700 [     main] [printAllState] threadState=TERMINATED
16:59:57.701 [     main] [printAllState] threadState=TERMINATED
16:59:57.701 [     main] [startConsumer]
16:59:57.702 [     소비자1] [run-소비 시도] ? in [data1, data2]
16:59:57.702 [     소비자1] [run-소비 완료] data1 <- [data2]
16:59:57.810 [     소비자2] [run-소비 시도] ? in [data2]
16:59:57.810 [     소비자2] [run-소비 완료] data2 <- []
16:59:57.920 [     소비자3] [run-소비 시도] ? in []
16:59:57.920 [     소비자3] [take] 큐가 비어 있음, 반환: null
16:59:57.920 [     소비자3] [run-소비 완료] null <- []
16:59:58.029 [     main] [printAllState] queue=[]
16:59:58.030 [     main] [printAllState] threadState=TERMINATED
16:59:58.030 [     main] [printAllState] threadState=TERMINATED
16:59:58.030 [     main] [printAllState] threadState=TERMINATED
16:59:58.030 [     main] [printAllState] threadState=TERMINATED
16:59:58.030 [     main] [printAllState] threadState=TERMINATED
16:59:58.030 [     main] [printAllState] threadState=TERMINATED
16:59:58.030 [     main] [consumerFirst] queueName=BoundedQueue1
16:59:58.030 [     main] [startConsumer]
16:59:58.030 [     소비자1] [run-소비 시도] ? in []
16:59:58.030 [     소비자1] [take] 큐가 비어 있음, 반환: null
16:59:58.030 [     소비자1] [run-소비 완료] null <- []
16:59:58.137 [     소비자2] [run-소비 시도] ? in []
16:59:58.137 [     소비자2] [take] 큐가 비어 있음, 반환: null
16:59:58.137 [     소비자2] [run-소비 완료] null <- []
16:59:58.248 [     소비자3] [run-소비 시도] ? in []
16:59:58.250 [     소비자3] [take] 큐가 비어 있음, 반환: null
16:59:58.250 [     소비자3] [run-소비 완료] null <- []
16:59:58.358 [     main] [printAllState] queue=[]
16:59:58.358 [     main] [printAllState] threadState=TERMINATED
16:59:58.358 [     main] [printAllState] threadState=TERMINATED
16:59:58.358 [     main] [printAllState] threadState=TERMINATED
16:59:58.358 [     main] [startProducer]
16:59:58.358 [     생산자1] [run-생산 시도] data1 -> []
16:59:58.358 [     생산자1] [run-생산 완료] data1 in [data1]
16:59:58.467 [     생산자2] [run-생산 시도] data2 -> [data1]
16:59:58.467 [     생산자2] [run-생산 완료] data2 in [data1, data2]
16:59:58.579 [     생산자3] [run-생산 시도] data3 -> [data1, data2]
16:59:58.579 [     생산자3] [put] 큐가 가득 참, 버림: data3
16:59:58.579 [     생산자3] [run-생산 완료] data3 in [data1, data2]
16:59:58.685 [     main] [printAllState] queue=[data1, data2]
16:59:58.686 [     main] [printAllState] threadState=TERMINATED
16:59:58.686 [     main] [printAllState] threadState=TERMINATED
16:59:58.686 [     main] [printAllState] threadState=TERMINATED
16:59:58.686 [     main] [printAllState] threadState=TERMINATED
16:59:58.686 [     main] [printAllState] threadState=TERMINATED
16:59:58.686 [     main] [printAllState] threadState=TERMINATED
 */