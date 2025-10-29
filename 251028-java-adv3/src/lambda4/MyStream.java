package lambda4;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class MyStream {
    private List<Integer> integerList;
    public MyStream(List<Integer> integerList) {
        this.integerList = integerList;
    }
    public MyStream filter(Predicate<Integer> predicate) {
        List<Integer> result = new ArrayList<>();
        for (Integer integer : integerList) {
            if (predicate.test(integer)) {
                result.add(integer);
            }
        }
        return new MyStream(result);
    }
    public MyStream mapper(Function<Integer, Integer> function) {
        List<Integer> result = new ArrayList<>();
        for (Integer integer : integerList) {
            result.add(function.apply(integer));
        }
        return new MyStream(result);
    }
    public List<Integer> toList() {
        return integerList;
    }
}
