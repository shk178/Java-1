package bounded;

import java.util.ArrayDeque;
import java.util.Queue;

import static thread.MyLogger.log;

public class BoundedQueue1 implements BoundedQueue {
    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;
    public BoundedQueue1(int max) {
        this.max = max;
    }
    @Override
    public synchronized void put(String data) {
        if (queue.size() == max) {
            log("[put] 큐가 가득 참, 버림: " + data);
            return;
        }
        queue.offer(data);
    }
    @Override
    public synchronized String take() {
        if (queue.isEmpty()) {
            log("[take] 큐가 비어 있음, 반환: null");
            return null;
        }
        return queue.poll();
    }
    @Override
    public synchronized String toString() {
        return queue.toString();
    }
}
