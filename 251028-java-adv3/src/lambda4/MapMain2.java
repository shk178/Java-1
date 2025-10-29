package lambda4;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapMain2 {
    public static void main(String[] args) {
        InnerMap innerMap = new InnerMap();
        List<String> list = List.of("-3", "a", "-1", "1", "2", "b", "5");
        List<Integer> result = innerMap.method(
                list,
                s -> {
                    try {
                        Integer.parseInt(s);
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                },
                str -> Integer.parseInt(str)
        );
        System.out.println(result); // [-3, -1, 1, 2, 5]
    }
    private static class InnerMap {
        <T, R> List<R> method(
                List<T> list,
                Predicate<T> predicate,
                Function<T, R> function
        ) {
            List<R> result = new ArrayList<>();
            for (T t : list) {
                if (predicate.test(t)) {
                    result.add(function.apply(t));
                }
            }
            return result;
        }
    }
}
