package msq;
import java.util.*;

public class MapMain {
    public static void main(String[] args) {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("A", 2);
        scores.put("B", 1);
        System.out.println(scores.get("A")); //2
        Set<String> keySet = scores.keySet();
        System.out.println(keySet); //[A, B]
        System.out.println(scores.values()); //[2, 1]
        //Set<Integer> vSet = scores.values();
        //java: incompatible types: java.util.Collection<java.lang.Integer> cannot be converted to java.util.Set<java.lang.Integer>
        Set<Integer> vSet = new HashSet<>(scores.values());
        System.out.println(vSet); //[1, 2]
        List<Integer> vList = new ArrayList<>(scores.values());
        System.out.println(vList); //[2, 1]
    }
}
