package lambda4;

import java.util.List;

public class StreamMain2 {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7);
        MyStream2 two = MyStream2.of(list);
        List<Integer> result = two.filter(i -> i % 2 == 0)
                .mapper(i -> i * 2)
                .toList();
        System.out.println(result); // [4, 8, 12]
    }
}
