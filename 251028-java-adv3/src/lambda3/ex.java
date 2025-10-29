package lambda3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ex {
    static List<Integer> filter(List<Integer> list, Predicate<Integer> predicate) {
        List<Integer> result = new ArrayList<>();
        for (Integer i : list) {
            if (predicate.test(i)) {
                result.add(i);
            }
        }
        return result;
    }
    public static void main(String[] args) {
        List<Integer> numbers = List.of(-3, -2, -1, 1, 2, 3, 5);
        List<Integer> negatives = filter(numbers, value -> value < 0);
        List<Integer> evens = filter(numbers, value -> value % 2 == 0);
        System.out.println(negatives); // [-3, -2, -1]
        System.out.println(evens); // [-2, 2]
    }
}
