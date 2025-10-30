package stream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FlatMap {
    public static void main(String[] args) {
        List<List<Integer>> outerList = List.of(
                List.of(1, 2),
                List.of(3, 4),
                List.of(5, 6)
        );
        System.out.println(outerList); // [[1, 2], [3, 4], [5, 6]]
        List<Integer> result1 = new ArrayList<>();
        for (List<Integer> list : outerList) {
            for (Integer integer: list) {
                result1.add(integer);
            }
        }
        System.out.println(result1); // [1, 2, 3, 4, 5, 6]
        List<Stream<Integer>> result2 =
                outerList.stream()
                        .map(list -> list.stream())
                        .toList();
        System.out.println(result2);
        // [java.util.stream.ReferencePipeline$Head@34c45dca,
        // java.util.stream.ReferencePipeline$Head@52cc8049,
        // java.util.stream.ReferencePipeline$Head@5b6f7412] (한 줄 출력)
        for (Stream<Integer> integerStream : result2) {
            System.out.println(integerStream.toList());
        }
        //[1, 2]
        //[3, 4]
        //[5, 6] (각 줄 출력)
        List<Integer> result3 =
                outerList.stream()
                        .flatMap(list -> list.stream())
                        .toList();
        System.out.println(result3); // [1, 2, 3, 4, 5, 6]
    }
}
