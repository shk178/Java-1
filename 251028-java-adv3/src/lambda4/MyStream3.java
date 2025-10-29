package lambda4;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MyStream3<T> {
    private List<T> list;
    private MyStream3(List<T> list) {
        this.list = list;
    }
    public static <S> MyStream3<S> of(List<S> list) {
        return new MyStream3(list);
    }
    public MyStream3<T> filter(Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T t : list) {
            if(predicate.test(t)) {
                result.add(t);
            }
        }
        return MyStream3.of(result);
    }
    public <R> MyStream3<R> mapper(Function<T, R> function) {
        List<R> result = new ArrayList<>();
        for (T t : list) {
            result.add(function.apply(t));
        }
        return MyStream3.of(result);
    }
    public List<T>  toList() {
        return list;
    }
    public void forEach(Consumer<T> consumer) {
        for (T t : list) {
            consumer.accept(t);
        }
    }
}
