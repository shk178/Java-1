package msq.queue;
import java.util.*;

public class ex1 {
    public static void main(String[] args) {
        TaskScheduler scheduler = new TaskScheduler();
        scheduler.addTask(new CleanTask());
        scheduler.addTask(new BackupTask());
        scheduler.addTask(new CompressionTask());
        run(scheduler);
    }
    public static void run(TaskScheduler scheduler) {
        while (scheduler.getRemainingTasks() > 0) {
            scheduler.processNextTask();
        }
    }
}

