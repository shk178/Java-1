package set2;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Set<String> hashSet = new HashSet<>();
        Set<String> linkedHashSet = new LinkedHashSet<>();
        Set<String> treeSet = new TreeSet<>();
        run(hashSet); //HashSet[a, 1, *]188
        run(linkedHashSet); //LinkedHashSet[*, a, 1]188
        run(treeSet); //TreeSet[*, 1, a]188
        hashSet.add("q");
        run(hashSet); //HashSet[a, 1, q, *]301
        linkedHashSet.add("w");
        run(linkedHashSet); //LinkedHashSet[*, a, 1, w]307
        treeSet.add("e");
        run(treeSet); //TreeSet[*, 1, a, e]289
        iter(hashSet); //a 1 q *
        iter(linkedHashSet); //* a 1 w
        iter(treeSet); //* 1 a e
    }
    private static void run(Set<String> set) {
        System.out.print(set.getClass().getSimpleName());
        set.add("*");
        set.add("a");
        set.add("1");
        System.out.print(set);
        System.out.println(set.hashCode());
    }
    private static void iter(Set<String> set) {
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
    }
}
