package iter1;

import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        MyArr arr = new MyArr(new int[]{10, 20, 30});
        Iterator<Integer> iterator = arr.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        System.out.println();
        for (Integer n : arr) {
            System.out.println(n);
        }
    }
}
/*
hasNext next hasNext 10
hasNext next hasNext 20
hasNext next hasNext 30
hasNext
hasNext next hasNext 10
hasNext next hasNext 20
hasNext next hasNext 30
hasNext
 */