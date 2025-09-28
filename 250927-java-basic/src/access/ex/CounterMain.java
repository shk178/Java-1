package access.ex;

public class CounterMain {
    public static void main(String[] args) {
        MaxCounter maxCounter = new MaxCounter(5);
        maxCounter.increment();
        maxCounter.increment();
        maxCounter.increment();
        maxCounter.increment();
        maxCounter.increment();
        maxCounter.increment();
        System.out.println(maxCounter.getCount());
    }
}
