package msq;
import java.util.*;

public class StackMain {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println(stack.peek()); //3 (다음에 꺼낼 거 조회)
        System.out.println(stack.peek()); //3
        System.out.println(stack.pop()); //3 (꺼냄)
        System.out.println(stack.pop()); //2
        System.out.println(stack.pop()); //1
        //System.out.println(stack.pop());
        //Exception in thread "main" java.util.EmptyStackException
        //	at java.base/java.util.Stack.peek(Stack.java:103)
        //	at java.base/java.util.Stack.pop(Stack.java:85)
        //	at msq.StackMain.main(StackMain.java:13)
    }
}
