package bounded;

import java.util.ArrayList;
import java.util.List;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class BoundedMain2 {
    public static void main(String[] args) {
        //1. BoundedQueue 선택
        BoundedQueue queue1 = new BoundedQueue2(2);
        BoundedQueue queue2 = new BoundedQueue2(2);
        //2. 생산자, 소비자 실행 순서 선택: 반드시 하나만 선택
        //producerFirst(queue1); //생산자 먼저 실행
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
17:23:51.044 [     main] [producerFirst] queueName=BoundedQueue2
17:23:51.046 [     main] [startProducer]
17:23:51.055 [     생산자1] [run-생산 시도] data1 -> []
17:23:51.055 [     생산자1] [run-생산 완료] data1 in [data1]
17:23:51.155 [     생산자2] [run-생산 시도] data2 -> [data1]
17:23:51.155 [     생산자2] [run-생산 완료] data2 in [data1, data2]
17:23:51.266 [     생산자3] [run-생산 시도] data3 -> [data1, data2]
17:23:51.266 [     생산자3] [put] 큐가 가득 참, 대기 //소비자가 없어서 멈춤
 */
/*
17:25:10.569 [     main] [consumerFirst] queueName=BoundedQueue2
17:25:10.571 [     main] [startConsumer]
17:25:10.575 [     소비자1] [run-소비 시도] ? in []
17:25:10.575 [     소비자1] [take] 큐가 비어 있음, 대기 //생산자가 없어서 멈춤
 */