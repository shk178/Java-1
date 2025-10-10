package hash;

import java.util.ArrayList;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        HashSet3<String> set = new HashSet3<>(10);
        set.add("ㄱ");
        set.add("a");
        set.add("ㅏ");
        set.add("b");
        set.add(".");
        set.printAll();
    }
}
/*
//몇 번을 해도 같게 나옴
Bucket 0: []
Bucket 1: []
Bucket 2: []
Bucket 3: [ㄱ, ㅏ]
Bucket 4: []
Bucket 5: []
Bucket 6: [.]
Bucket 7: [a]
Bucket 8: [b]
Bucket 9: []
 */