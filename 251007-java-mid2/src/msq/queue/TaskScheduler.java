package msq.queue;
import java.util.ArrayDeque;
import java.util.Deque;

public class TaskScheduler {
    private Deque<Task> queue = new ArrayDeque<>();
    private int size = 0;
    public void addTask(Task t) {
        queue.offer(t);
        size++;
    }
    public int getRemainingTasks() {
        return size;
    }
    public void processNextTask() {
        if (queue.peek() != null) {
            Task t = queue.pop();
            size--;
            t.execute();
        }
    }
}
