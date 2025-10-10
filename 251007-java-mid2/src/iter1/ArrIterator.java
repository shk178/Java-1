package iter1;
import java.util.*;

public class ArrIterator implements Iterator<Integer> {
    private int currentIndex = -1;
    private int[] targetArr;
    ArrIterator(int[] targetArr) {
        this.targetArr = targetArr;
    }
    @Override
    public boolean hasNext() {
        System.out.print("hasNext ");
        return currentIndex < targetArr.length - 1;
    }
    @Override
    public Integer next() {
        System.out.print("next ");
        if (hasNext()) {
            Integer result = targetArr[++currentIndex];
            return result;
            //++가 먼저 일어난 후에 인덱스로 접근
        }
        return null;
    }
}
