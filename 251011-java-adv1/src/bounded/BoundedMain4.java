package bounded;

import java.util.ArrayList;
import java.util.List;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class BoundedMain4 {
    public static void main(String[] args) {
        //1. BoundedQueue 선택
        BoundedQueue queue1 = new BoundedQueue4(2);
        BoundedQueue queue2 = new BoundedQueue4(2);
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
19:17:50.996 [     main] [producerFirst] queueName=BoundedQueue4
19:17:50.998 [     main] [startProducer]
19:17:51.010 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
19:17:51.111 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
19:17:51.221 [     생산자3] [put] 큐가 가득 참, 대기, while문 실행 횟수=1
19:17:51.329 [     main] [printAllState] queue=[data1, data2]
19:17:51.329 [     main] [printAllState] threadState=TERMINATED
19:17:51.330 [     main] [printAllState] threadState=TERMINATED
19:17:51.330 [     main] [printAllState] threadState=WAITING
19:17:51.330 [     main] [startConsumer]
19:17:51.331 [     소비자1] [take] poll result=data1, while문 실행 횟수=0
19:17:51.331 [     생산자3] [put] offer data=data3, while문 실행 횟수=1
19:17:51.437 [     소비자2] [take] poll result=data2, while문 실행 횟수=0
19:17:51.546 [     소비자3] [take] poll result=data3, while문 실행 횟수=0
19:17:51.655 [     main] [printAllState] queue=[]
19:17:51.655 [     main] [printAllState] threadState=TERMINATED
19:17:51.655 [     main] [printAllState] threadState=TERMINATED
19:17:51.655 [     main] [printAllState] threadState=TERMINATED
19:17:51.655 [     main] [printAllState] threadState=TERMINATED
19:17:51.656 [     main] [printAllState] threadState=TERMINATED
19:17:51.656 [     main] [printAllState] threadState=TERMINATED
19:17:51.656 [     main] [consumerFirst] queueName=BoundedQueue4
19:17:51.656 [     main] [startConsumer]
19:17:51.657 [     소비자1] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:17:51.765 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:17:51.872 [     소비자3] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:17:51.980 [     main] [printAllState] queue=[]
19:17:51.980 [     main] [printAllState] threadState=WAITING
19:17:51.981 [     main] [printAllState] threadState=WAITING
19:17:51.981 [     main] [printAllState] threadState=WAITING
19:17:51.981 [     main] [startProducer]
19:17:51.982 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
19:17:51.982 [     소비자1] [take] poll result=data1, while문 실행 횟수=1
19:17:51.982 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=2
19:17:52.090 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
19:17:52.090 [     소비자3] [take] poll result=data2, while문 실행 횟수=1
19:17:52.091 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=3
19:17:52.198 [     생산자3] [put] offer data=data3, while문 실행 횟수=0
19:17:52.199 [     소비자2] [take] poll result=data3, while문 실행 횟수=3
19:17:52.306 [     main] [printAllState] queue=[]
19:17:52.306 [     main] [printAllState] threadState=TERMINATED
19:17:52.306 [     main] [printAllState] threadState=TERMINATED
19:17:52.307 [     main] [printAllState] threadState=TERMINATED
19:17:52.307 [     main] [printAllState] threadState=TERMINATED
19:17:52.307 [     main] [printAllState] threadState=TERMINATED
19:17:52.308 [     main] [printAllState] threadState=TERMINATED
 */