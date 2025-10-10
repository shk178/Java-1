package msq;
import java.util.*;

public class DequeMain {
    public static void main(String[] args) {
        //stack으로 사용
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1); //addFirst()
        stack.push(2);
        stack.pop(); //removeFirst()  → 2
        //queue로 사용
        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(1); //addLast()
        queue.offer(2);
        queue.poll(); //removeFirst()  → 1
        //양방향 큐로 사용
        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("A"); //[A]
        deque.addLast("B"); //[A, B]
        deque.addFirst("Z"); //[Z, A, B]
        deque.removeLast(); //[Z, A]
    }
}
