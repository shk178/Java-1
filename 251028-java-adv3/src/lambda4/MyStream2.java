package lambda4;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class MyStream2 {
    private List<Integer> internalList;
    private MyStream2(List<Integer> integerList) {
        this.internalList = integerList;
    }
    public static MyStream2 of(List<Integer> integerList) {
        return new MyStream2(integerList);
    }
    public MyStream2 filter(Predicate<Integer> predicate) {
        List<Integer> result = new ArrayList<>();
        for (Integer integer : internalList) {
            if(predicate.test(integer)) {
                result.add(integer);
            }
        }
        return MyStream2.of(result);
    }
    public MyStream2 mapper(Function<Integer, Integer> function) {
        List<Integer> result = new ArrayList<>();
        for (Integer integer : internalList) {
            result.add(function.apply(integer));
        }
        return MyStream2.of(result);
    }
    public List<Integer>  toList() {
        return internalList;
    }
}
