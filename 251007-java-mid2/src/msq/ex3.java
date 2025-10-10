package msq;
import java.util.*;

public class ex3 {
    public static void main(String[] args) {
        String t = "oran bana appl appl bana appl";
        Map<String, Integer> map = new HashMap<>();
        String[] words = t.split(" ");
        for (String word : words) {
            map.put(word, map.computeIfAbsent(word, k -> 0) + 1);
        }
        System.out.println(map);
    }
}
