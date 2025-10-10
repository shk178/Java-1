package set2;
import java.util.*;

public class ex1 {
    public static void main(String[] args) {
        Integer[] inputArr = {30, 20, 20, 10, 10};
        Set<Integer> set = new HashSet<>();
        for (Integer i : inputArr) {
            set.add(i);
        }
        System.out.println(set); //[20, 10, 30]
        set = new LinkedHashSet<>(List.of(inputArr)); //배열을 list로
        System.out.println(set); //[30, 20, 10]
        set = new TreeSet<>(List.of(inputArr));
        System.out.println(set); //[10, 20, 30]
    }
}
