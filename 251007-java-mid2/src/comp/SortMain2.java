package comp;
import java.util.*;

public class SortMain2 {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(10);
        list.add(2);
        list.add(3);
        System.out.println(list); //[10, 2, 3]
        System.out.println(Collections.max(list)); //10
        System.out.println(Collections.min(list)); //2
        Collections.shuffle(list);
        System.out.println(list); //[2, 10, 3] or [10, 2, 3] or [3, 2, 10]
        List<Integer> list1 = List.of(1, 2, 3);
        Set<Integer> set1 = Set.of(1, 2, 3);
        Map<Integer, String> map1 = Map.of(1, "c", 2, "b", 3, "a");
        System.out.println(list1.getClass()); //class java.util.ImmutableCollections$ListN
        System.out.println(set1.getClass()); //class java.util.ImmutableCollections$SetN
        System.out.println(map1.getClass()); //class java.util.ImmutableCollections$MapN
        List<Integer> list2 = new ArrayList<>(list1);
        Set<Integer> set2 = new HashSet<>(set1);
        Map<Integer, String> map2 = new HashMap<>(map1);
        System.out.println(list2.getClass()); //class java.util.ArrayList
        System.out.println(set2.getClass()); //class java.util.HashSet
        System.out.println(map2.getClass()); //class java.util.HashMap
        List<Integer> list3 = Arrays.asList(1, 2, 3);
        System.out.println(list3.getClass()); //class java.util.Arrays$ArrayList
        //list3.add(0);
        System.out.println(list3);
        //Exception in thread "main" java.lang.UnsupportedOperationException
        //	at java.base/java.util.AbstractList.add(AbstractList.java:155)
        //	at java.base/java.util.AbstractList.add(AbstractList.java:113)
        //	at comp.SortMain2.main(SortMain2.java:29)
    }
}
