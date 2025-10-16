package bounded2;

import bounded.BoundedQueue;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static thread.MyLogger.log;

public class Bounded2Queue implements BoundedQueue {
    private final Lock lock = new ReentrantLock(true);
    private final Condition condition = lock.newCondition();
    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;
    public Bounded2Queue(int max) {
        this.max = max;
    }
    @Override
    public void put(String data) {
        int count = 0;
        lock.lock();
        try {
            while (queue.size() == max) {
                log("[put] 큐가 가득 참, 대기, while문 실행 횟수=" + ++count);
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.offer(data);
            log("[put] offer data=" + data + ", while문 실행 횟수=" + count);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public String take() {
        int count = 0;
        String result;
        lock.lock();
        try {
            while (queue.isEmpty()) {
                log("[take] 큐가 비어 있음, 대기, while문 실행 횟수=" + ++count);
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            result = queue.poll();
            log("[take] poll result=" + result + ", while문 실행 횟수=" + count);
            condition.signal();
        } finally {
            lock.unlock();
        }
        return result;
    }
    @Override
    public String toString() {
        return queue.toString();
    }
}