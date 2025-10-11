package thread;

public class ex {
    public static void main(String[] args) {
        Thread t = new Thread(new CounterRunnable(), "counter");
        t.start();
    }
}
/*
21:27:28.541 [  counter] 1
21:27:29.549 [  counter] 2
21:27:30.563 [  counter] 3
21:27:31.577 [  counter] 4
21:27:32.592 [  counter] 5
 */