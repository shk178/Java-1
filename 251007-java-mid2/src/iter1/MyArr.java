package iter1;

import java.util.Iterator;

public class MyArr implements Iterable<Integer> {
    private int[] numbers;
    public MyArr(int[] numbers) {
        this.numbers = numbers;
    }
    @Override
    public Iterator<Integer> iterator() {
        return new ArrIterator(numbers);
    }
}
