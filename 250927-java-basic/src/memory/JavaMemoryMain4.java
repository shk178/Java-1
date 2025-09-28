package memory;

public class JavaMemoryMain4 {
    public static void main(String[] args) {
        Counter counter = new Counter();
        Data2 data1 = new Data2("A", counter);
        Data2 data2 = new Data2("B", counter);
        System.out.println("counter.count = " + counter.count);
    }
}
