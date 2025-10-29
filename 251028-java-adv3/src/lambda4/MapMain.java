package lambda4;

import java.util.ArrayList;
import java.util.List;

public class MapMain {
    public static void main(String[] args) {
        List<String> list = List.of("1", "12", "123");
        ListMapInterface<String, Integer> one = (inputList) -> {
            List<Integer> result = new ArrayList<>();
            for (String t : inputList) {
                result.add(Integer.parseInt(t));
            }
            return result;
        };
        System.out.println(one.listMap(list)); // [1, 12, 123]
        ListMapInterface2 two = new ListMapInterface2() {
            @Override
            public <T, R> List<R> listMap(List<T> list) {
                List<R> result = new ArrayList<>();
                for (T t : list) {
                    result.add((R) Integer.valueOf(t.toString()));
                }
                return result;
            }
        };
        System.out.println(two.<String, Integer>listMap(list)); // [1, 12, 123]
    }
    @FunctionalInterface
    private interface ListMapInterface<T, R> {
        List<R> listMap(List<T> list);
    }
    @FunctionalInterface
    private interface ListMapInterface2 {
        <T, R> List<R> listMap(List<T> list);
    }
}
