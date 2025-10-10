package msq.stack;
import java.util.ArrayDeque;
import java.util.Deque;

public class NetHistory {
    private Deque<String> pages = new ArrayDeque<>();
    private String now;
    public void visitPage(String s) {
        if (now != null) {
            pages.push(now);
        }
        now = s;
        System.out.println("visitPage to: " + now);
    }
    public void goBack() {
        if (pages.peek() != null) {
            now = pages.pop();
        } else {
            now = null;
        }
        System.out.println("goBack to: " + now);
    }
}
