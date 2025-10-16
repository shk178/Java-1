package bounded2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class Main {
    public static void main(String[] args) {
        BlockingQueue<String> queue1 = new ArrayBlockingQueue<>(2);
        BlockingQueue<String> queue2 = new ArrayBlockingQueue<>(2);
        producerFirst(queue1);
        consumerFirst(queue2);
    }
    private static void producerFirst(BlockingQueue<String> queue) {
        log("[producerFirst] queueName=" + queue.getClass().getSimpleName());
        List<Thread> threads = new ArrayList<>();
        startProducer(queue, threads);
        printAllState(queue, threads);
        startConsumer(queue, threads);
        printAllState(queue, threads);
    }
    private static void consumerFirst(BlockingQueue<String> queue) {
        log("[consumerFirst] queueName=" + queue.getClass().getSimpleName());
        List<Thread> threads = new ArrayList<>();
        startConsumer(queue, threads);
        printAllState(queue, threads);
        startProducer(queue, threads);
        printAllState(queue, threads);
    }
    private static void startProducer(BlockingQueue<String> queue, List<Thread> threads) {
        log("[startProducer]");
        for (int i = 1; i < 4; i++) {
            Thread producer = new Thread(new ProducerTask2(queue, "data" + i), "생산자" + i);
            threads.add(producer);
            producer.start();
            sleep(100);
        }
    }
    private static void startConsumer(BlockingQueue<String> queue, List<Thread> threads) {
        log("[startConsumer]");
        for (int i = 1; i < 4; i++) {
            Thread consumer = new Thread(new ConsumerTask2(queue), "소비자" + i);
            threads.add(consumer);
            consumer.start();
            sleep(100);
        }
    }
    private static void printAllState(BlockingQueue<String> queue, List<Thread> threads) {
        log("[printAllState] queue=" + queue);
        for (Thread thread : threads) {
            log("[printAllState] threadState=" + thread.getState());
        }
    }
    static class ProducerTask2 implements Runnable {
        private final BlockingQueue<String> queue;
        private final String data;
        public ProducerTask2(BlockingQueue<String> queue, String data) {
            this.queue = queue;
            this.data = data;
        }
        @Override
        public void run() {
            try {
                queue.put(data); //큐가 가득 차면 대기
                log("[put] data=" + data + ", queue=" + queue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    static class ConsumerTask2 implements Runnable {
        private final BlockingQueue<String> queue;
        public ConsumerTask2(BlockingQueue<String> queue) {
            this.queue = queue;
        }
        @Override
        public void run() {
            try {
                String result = queue.take(); //큐가 비면 대기
                log("[take] result=" + result + ", queue=" + queue);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
/*
21:55:22.363 [     main] [producerFirst] queueName=ArrayBlockingQueue
21:55:22.365 [     main] [startProducer]
21:55:22.375 [     생산자1] [put] data=data1, queue=[data1]
21:55:22.483 [     생산자2] [put] data=data2, queue=[data1, data2]
21:55:22.702 [     main] [printAllState] queue=[data1, data2]
21:55:22.702 [     main] [printAllState] threadState=TERMINATED
21:55:22.702 [     main] [printAllState] threadState=TERMINATED
21:55:22.703 [     main] [printAllState] threadState=WAITING
21:55:22.703 [     main] [startConsumer]
21:55:22.704 [     생산자3] [put] data=data3, queue=[data2, data3]
21:55:22.704 [     소비자1] [take] result=data1, queue=[data2]
21:55:22.813 [     소비자2] [take] result=data2, queue=[data3]
21:55:22.921 [     소비자3] [take] result=data3, queue=[]
21:55:23.030 [     main] [printAllState] queue=[]
21:55:23.030 [     main] [printAllState] threadState=TERMINATED
21:55:23.030 [     main] [printAllState] threadState=TERMINATED
21:55:23.030 [     main] [printAllState] threadState=TERMINATED
21:55:23.030 [     main] [printAllState] threadState=TERMINATED
21:55:23.030 [     main] [printAllState] threadState=TERMINATED
21:55:23.032 [     main] [printAllState] threadState=TERMINATED
21:55:23.032 [     main] [consumerFirst] queueName=ArrayBlockingQueue
21:55:23.032 [     main] [startConsumer]
21:55:23.359 [     main] [printAllState] queue=[]
21:55:23.359 [     main] [printAllState] threadState=WAITING
21:55:23.359 [     main] [printAllState] threadState=WAITING
21:55:23.359 [     main] [printAllState] threadState=WAITING
21:55:23.359 [     main] [startProducer]
21:55:23.360 [     생산자1] [put] data=data1, queue=[data1]
21:55:23.360 [     소비자1] [take] result=data1, queue=[]
21:55:23.467 [     소비자2] [take] result=data2, queue=[]
21:55:23.467 [     생산자2] [put] data=data2, queue=[data2]
21:55:23.578 [     소비자3] [take] result=data3, queue=[]
21:55:23.578 [     생산자3] [put] data=data3, queue=[data3]
21:55:23.687 [     main] [printAllState] queue=[]
21:55:23.687 [     main] [printAllState] threadState=TERMINATED
21:55:23.687 [     main] [printAllState] threadState=TERMINATED
21:55:23.687 [     main] [printAllState] threadState=TERMINATED
21:55:23.687 [     main] [printAllState] threadState=TERMINATED
21:55:23.689 [     main] [printAllState] threadState=TERMINATED
21:55:23.689 [     main] [printAllState] threadState=TERMINATED
 */