package bounded2;

public class ProducerTask implements Runnable {
    private BoundedQueue queue;
    private String request;
    public ProducerTask(BoundedQueue queue, String request) {
        this.queue = queue;
        this.request = request;
    }
    @Override
    public void run() {
        //log("[run-생산 시도] " + request + " -> " + queue);
        queue.put(request);
        //log("[run-생산 완료] " + request + " in " + queue);
    }
}
