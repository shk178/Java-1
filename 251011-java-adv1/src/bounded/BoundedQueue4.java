package bounded;

import java.util.ArrayDeque;
import java.util.Queue;
import static thread.MyLogger.log;

public class BoundedQueue4 implements BoundedQueue {
    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;
    public BoundedQueue4(int max) {
        this.max = max;
    }
    @Override
    public synchronized void put(String data) {
        int count = 0;
        while (queue.size() == max) {
            log("[put] 큐가 가득 참, 대기, while문 실행 횟수=" + ++count);
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        queue.offer(data);
        log("[put] offer data=" + data + ", while문 실행 횟수=" + count);
        this.notify();
    }
    @Override
    public synchronized String take() {
        int count = 0;
        while (queue.isEmpty()) {
            log("[take] 큐가 비어 있음, 대기, while문 실행 횟수=" + ++count);
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        String result = queue.poll();
        log("[take] poll result=" + result + ", while문 실행 횟수=" + count);
        this.notify();
        return result;
    }
    @Override
    public synchronized String toString() {
        return queue.toString();
    }
}