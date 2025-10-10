package msq;
import java.util.*;

public class ex2 {
    public static void main(String[] args) {
        Map<String, Integer> map1 = new HashMap<>();
        map1.put("A", 1);
        map1.put("B", 2);
        map1.put("C", 3);
        Map<String, Integer> map2 = new HashMap<>();
        map2.put("B", 4);
        map2.put("C", 5);
        map2.put("D", 6);
        for (String s : map1.keySet()) {
            if (map2.containsKey(s)) {
                System.out.print("k=" + s + " ");
                System.out.print("sum(v)=");
                System.out.println(map1.get(s) + map2.get(s));
            }
        }
    }
}
