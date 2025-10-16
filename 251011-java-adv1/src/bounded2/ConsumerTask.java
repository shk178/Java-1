package bounded2;

public class ConsumerTask implements Runnable {
    private BoundedQueue queue;
    public ConsumerTask(BoundedQueue queue) {
        this.queue = queue;
    }
    @Override
    public void run() {
        //log("[run-소비 시도] " + "? in " + queue);
        String data = queue.take();
        //log("[run-소비 완료] " + data + " <- " + queue);
    }
}
