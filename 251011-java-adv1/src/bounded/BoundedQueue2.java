package bounded;

import java.util.ArrayDeque;
import java.util.Queue;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class BoundedQueue2 implements BoundedQueue {
    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;
    public BoundedQueue2(int max) {
        this.max = max;
    }
    @Override
    public synchronized void put(String data) {
        if (queue.size() == max) {
            log("[put] 큐가 가득 참, 대기");
            while (queue.size() == max) {
                sleep(1000);
            }
        }
        queue.offer(data);
    }
    @Override
    public synchronized String take() {
        if (queue.isEmpty()) {
            log("[take] 큐가 비어 있음, 대기");
            while (queue.isEmpty()) {
                sleep(1000);
            }
        }
        return queue.poll();
    }
    @Override
    public synchronized String toString() {
        return queue.toString();
    }
}
