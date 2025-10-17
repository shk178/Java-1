package cas;

public class II1 implements  IncrementInteger {
    private int value;
    @Override
    public void increment() {
        for (int i = 0; i < 100; i++) {
            value++;
        }
    }
    @Override
    public int get() {
        return value;
    }
}
