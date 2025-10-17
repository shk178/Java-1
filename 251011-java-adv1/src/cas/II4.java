package cas;

import java.util.concurrent.atomic.AtomicInteger;

public class II4 implements IncrementInteger {
    private AtomicInteger atomicValue = new AtomicInteger(0);
    @Override
    public void increment() {
        for (int i = 0; i < 100; i++) {
            atomicValue.incrementAndGet();
        }
    }
    @Override
    public int get() {
        return atomicValue.get();
    }
}
