package lambda2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ex3 {
    public static void main(String[] args) {
        List<Integer> list = List.of(-3, -2, -1, 1, 2, 3, 5);
        filter(list, (int n) -> {
            return n < 0; // 음수만
        }); // [-3, -2, -1]
        filter(list, (int n) -> {
            return n % 2 == 0; // 짝수만
        }); // [-2, 2]
    }
    static void filter(List<Integer> list, MyPredicate predicate) {
        List<Integer> result = new ArrayList<>();
        for (int i : list) {
            if (predicate.test(i)) {
                result.add(i);
            }
        }
        System.out.println(result);
    }
}
