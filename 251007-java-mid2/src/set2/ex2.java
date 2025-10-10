package set2;
import java.util.*;

public class ex2 {
    public static void main(String[] args) {
        Integer[] input1 = {1, 2, 3, 4, 5};
        Integer[] input2 = {3, 4, 5, 6, 7};
        Set<Integer> set1 = new HashSet<>(List.of(input1));
        Set<Integer> set2 = new HashSet<>(List.of(input2));
        //합집합
        Set<Integer> set3 = new HashSet<>(set1);
        for (Integer i : set2) {
            if (!set1.contains(i)) {
                set3.add(i);
            }
        }
        System.out.println(set3);
        //교집합
        Set<Integer> set4 = new HashSet<>();
        for (Integer i : set2) {
            if (set1.contains(i)) {
                set4.add(i);
            }
        }
        System.out.println(set4);
        //차집합
        Set<Integer> set5 = new HashSet<>(set1);
        for (Integer i : set2) {
            if (set1.contains(i)) {
                set5.remove(i);
            }
        }
        System.out.println(set5);
        //합집합: addAll() 모든 요소 추가 → 합집합
        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);
        System.out.println("합집합: " + union);
        //교집합: retainAll() 공통 요소만 유지 → 교집합
        Set<Integer> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        System.out.println("교집합: " + intersection);
        //차집합: removeAll() 해당 요소 모두 제거 → 차집합
        Set<Integer> difference = new HashSet<>(set1);
        difference.removeAll(set2);
        System.out.println("차집합: " + difference);
    }
}
/*
[1, 2, 3, 4, 5, 6, 7]
[3, 4, 5]
[1, 2]
합집합: [1, 2, 3, 4, 5, 6, 7]
교집합: [3, 4, 5]
차집합: [1, 2]
 */