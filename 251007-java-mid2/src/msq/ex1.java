package msq;
import java.util.*;

public class ex1 {
    public static void main(String[] args) {
        String[][] productArr = {{"j", "10"}, {"s", "20"}, {"a", "30"}};
        //Map 생성
        Map<String, Integer> productMap = new HashMap<>();
        for (String[] strings : productArr) {
            productMap.putIfAbsent(strings[0], Integer.valueOf(strings[1]));
        }
        //Map 출력
        System.out.println(productMap);
    }
}
