package msq;
import java.util.*;

public class ex4 {
    public static void main(String[] args) {
        //사전 저장
        Map<String, String> dict = new HashMap<>();
        dict.put("apple", "사과");
        dict.put("banana", "바나나");
        dict.put("grape", "포도");
        //사전 검색
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("단어: ");
            String key = input.nextLine();
            if (key.equals("exit")) {
                break;
            }
            if (dict.containsKey(key)) {
                System.out.println(dict.get(key));
            } else {
                System.out.print("뜻 입력: ");
                dict.put(key, input.nextLine());
            }
        }
    }
}
