package msq;
import java.util.*;

public class QueueMain {
    public static void main(String[] args) {
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        System.out.println(queue.peek()); //1
        System.out.println(queue.peek()); //1
        System.out.println(queue.poll()); //1
        System.out.println(queue.poll()); //2
        System.out.println(queue.poll()); //3
        System.out.println(queue.poll()); //null
        //System.out.println(queue.remove());
        //Exception in thread "main" java.util.NoSuchElementException
        //	at java.base/java.util.LinkedList.removeFirst(LinkedList.java:281)
        //	at java.base/java.util.LinkedList.remove(LinkedList.java:696)
        //	at msq.QueueMain.main(QueueMain.java:16)
    }
}
