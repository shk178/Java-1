package iter1;
import java.util.*;

public class IterMain {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        Set<Integer> set = new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
        printAll(list.iterator());
        printAll(set.iterator());
        foreach(list);
        foreach(set);
    }
    private static void printAll(Iterator<Integer> iterator) {
        System.out.println(iterator.getClass());
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
    private static void foreach(Iterable<Integer> iterable) {
        System.out.println(iterable.getClass());
        for (Integer i : iterable) {
            System.out.println(i);
        }
    }
}
/*
class java.util.ArrayList$Itr
1
2
3
class java.util.HashMap$KeyIterator
1
2
3
class java.util.ArrayList
1
2
3
class java.util.HashSet
1
2
3
 */