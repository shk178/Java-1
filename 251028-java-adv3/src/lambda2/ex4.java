package lambda2;

import java.util.List;

public class ex4 {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4);
        reduce(list, 0, (a, b) -> a + b); // 10
        reduce(list, 1, (a, b) -> a * b); // 24
    }
    static void reduce(List<Integer> list, int initial, MyReducer reducer) {
        int result = initial;
        for (int i : list) {
            result = reducer.reduce(result, i);
        }
        System.out.println(result);
    }
}
