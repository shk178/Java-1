package bounded;

import java.util.ArrayList;
import java.util.List;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class BoundedMain5 {
    public static void main(String[] args) {
        //1. BoundedQueue 선택
        BoundedQueue queue1 = new BoundedQueue5(2);
        BoundedQueue queue2 = new BoundedQueue5(2);
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
19:18:15.712 [     main] [producerFirst] queueName=BoundedQueue5
19:18:15.715 [     main] [startProducer]
19:18:15.725 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
19:18:15.833 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
19:18:15.944 [     생산자3] [put] 큐가 가득 참, 대기, while문 실행 횟수=1
19:18:16.054 [     main] [printAllState] queue=[data1, data2]
19:18:16.055 [     main] [printAllState] threadState=TERMINATED
19:18:16.055 [     main] [printAllState] threadState=TERMINATED
19:18:16.055 [     main] [printAllState] threadState=WAITING
19:18:16.055 [     main] [startConsumer]
19:18:16.056 [     소비자1] [take] poll result=data1, while문 실행 횟수=0
19:18:16.056 [     생산자3] [put] offer data=data3, while문 실행 횟수=1
19:18:16.163 [     소비자2] [take] poll result=data2, while문 실행 횟수=0
19:18:16.274 [     소비자3] [take] poll result=data3, while문 실행 횟수=0
19:18:16.382 [     main] [printAllState] queue=[]
19:18:16.382 [     main] [printAllState] threadState=TERMINATED
19:18:16.382 [     main] [printAllState] threadState=TERMINATED
19:18:16.382 [     main] [printAllState] threadState=TERMINATED
19:18:16.382 [     main] [printAllState] threadState=TERMINATED
19:18:16.383 [     main] [printAllState] threadState=TERMINATED
19:18:16.383 [     main] [printAllState] threadState=TERMINATED
19:18:16.383 [     main] [consumerFirst] queueName=BoundedQueue5
19:18:16.383 [     main] [startConsumer]
19:18:16.384 [     소비자1] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:18:16.492 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:18:16.602 [     소비자3] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:18:16.711 [     main] [printAllState] queue=[]
19:18:16.711 [     main] [printAllState] threadState=WAITING
19:18:16.712 [     main] [printAllState] threadState=WAITING
19:18:16.712 [     main] [printAllState] threadState=WAITING
19:18:16.712 [     main] [startProducer]
19:18:16.712 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
19:18:16.713 [     소비자1] [take] poll result=data1, while문 실행 횟수=1
19:18:16.713 [     소비자3] [take] 큐가 비어 있음, 대기, while문 실행 횟수=2
19:18:16.713 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=2
19:18:16.818 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
19:18:16.818 [     소비자3] [take] poll result=data2, while문 실행 횟수=2
19:18:16.820 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=3
19:18:16.928 [     생산자3] [put] offer data=data3, while문 실행 횟수=0
19:18:16.929 [     소비자2] [take] poll result=data3, while문 실행 횟수=3
19:18:17.036 [     main] [printAllState] queue=[]
19:18:17.037 [     main] [printAllState] threadState=TERMINATED
19:18:17.037 [     main] [printAllState] threadState=TERMINATED
19:18:17.037 [     main] [printAllState] threadState=TERMINATED
19:18:17.037 [     main] [printAllState] threadState=TERMINATED
19:18:17.037 [     main] [printAllState] threadState=TERMINATED
19:18:17.038 [     main] [printAllState] threadState=TERMINATED
 */