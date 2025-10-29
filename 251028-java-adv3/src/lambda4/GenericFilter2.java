package lambda4;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class GenericFilter2 {
    public static <T, R> List<R> filterAndMap(
            List<T> list,
            Predicate<T> predicate,
            Function<T, R> mapper // T를 R로 변환
    ) {
        List<R> result = new ArrayList<>();
        for (T t : list) {
            if (predicate.test(t)) {
                result.add(mapper.apply(t)); // T를 R로 변환해서 추가
            }
        }
        return result;
    }
}
