package bounded2;

import bounded.BoundedQueue;
import bounded.ConsumerTask;
import bounded.ProducerTask;

import java.util.ArrayList;
import java.util.List;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class Bounded2Main {
    public static void main(String[] args) {
        //1. BoundedQueue 선택
        bounded.BoundedQueue queue1 = new Bounded2Queue(2);
        bounded.BoundedQueue queue2 = new Bounded2Queue(2);
        //2. 생산자, 소비자 실행 순서 선택: 반드시 하나만 선택
        producerFirst(queue1); //생산자 먼저 실행
        consumerFirst(queue2); //소비자 먼저 실행
    }
    private static void producerFirst(bounded.BoundedQueue queue) {
        log("[producerFirst] queueName=" + queue.getClass().getSimpleName());
        List<Thread> threads = new ArrayList<>();
        startProducer(queue, threads);
        printAllState(queue, threads);
        startConsumer(queue, threads);
        printAllState(queue, threads);
    }
    private static void consumerFirst(bounded.BoundedQueue queue) {
        log("[consumerFirst] queueName=" + queue.getClass().getSimpleName());
        List<Thread> threads = new ArrayList<>();
        startConsumer(queue, threads);
        printAllState(queue, threads);
        startProducer(queue, threads);
        printAllState(queue, threads);
    }
    private static void startProducer(bounded.BoundedQueue queue, List<Thread> threads) {
        log("[startProducer]");
        for (int i = 1; i < 4; i++) {
            Thread producer = new Thread(new ProducerTask(queue, "data" + i), "생산자" + i);
            threads.add(producer);
            producer.start();
            sleep(100);
        }
    }
    private static void startConsumer(bounded.BoundedQueue queue, List<Thread> threads) {
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
19:51:06.064 [     main] [producerFirst] queueName=Bounded2Queue
19:51:06.066 [     main] [startProducer]
19:51:06.075 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
19:51:06.186 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
19:51:06.298 [     생산자3] [put] 큐가 가득 참, 대기, while문 실행 횟수=1
19:51:06.408 [     main] [printAllState] queue=[data1, data2]
19:51:06.408 [     main] [printAllState] threadState=TERMINATED
19:51:06.408 [     main] [printAllState] threadState=TERMINATED
19:51:06.409 [     main] [printAllState] threadState=WAITING
19:51:06.409 [     main] [startConsumer]
19:51:06.409 [     소비자1] [take] poll result=data1, while문 실행 횟수=0
19:51:06.409 [     생산자3] [put] offer data=data3, while문 실행 횟수=1
19:51:06.515 [     소비자2] [take] poll result=data2, while문 실행 횟수=0
19:51:06.624 [     소비자3] [take] poll result=data3, while문 실행 횟수=0
19:51:06.734 [     main] [printAllState] queue=[]
19:51:06.734 [     main] [printAllState] threadState=TERMINATED
19:51:06.734 [     main] [printAllState] threadState=TERMINATED
19:51:06.734 [     main] [printAllState] threadState=TERMINATED
19:51:06.734 [     main] [printAllState] threadState=TERMINATED
19:51:06.734 [     main] [printAllState] threadState=TERMINATED
19:51:06.734 [     main] [printAllState] threadState=TERMINATED
19:51:06.734 [     main] [consumerFirst] queueName=Bounded2Queue
19:51:06.735 [     main] [startConsumer]
19:51:06.735 [     소비자1] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:51:06.843 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:51:06.956 [     소비자3] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:51:07.063 [     main] [printAllState] queue=[]
19:51:07.063 [     main] [printAllState] threadState=WAITING
19:51:07.063 [     main] [printAllState] threadState=WAITING
19:51:07.063 [     main] [printAllState] threadState=WAITING
19:51:07.063 [     main] [startProducer]
19:51:07.063 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
19:51:07.065 [     소비자1] [take] poll result=data1, while문 실행 횟수=1
19:51:07.065 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=2
19:51:07.180 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
19:51:07.180 [     소비자3] [take] poll result=data2, while문 실행 횟수=1
19:51:07.180 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=3
19:51:07.286 [     생산자3] [put] offer data=data3, while문 실행 횟수=0
19:51:07.286 [     소비자2] [take] poll result=data3, while문 실행 횟수=3
19:51:07.389 [     main] [printAllState] queue=[]
19:51:07.389 [     main] [printAllState] threadState=TERMINATED
19:51:07.389 [     main] [printAllState] threadState=TERMINATED
19:51:07.389 [     main] [printAllState] threadState=TERMINATED
19:51:07.389 [     main] [printAllState] threadState=TERMINATED
19:51:07.389 [     main] [printAllState] threadState=TERMINATED
19:51:07.389 [     main] [printAllState] threadState=TERMINATED
 */