package comp;

import java.util.Arrays;

public class SortMain {
    public static void main(String[] args) {
        Integer[] arr = {3, 2, 1};
        System.out.println(Arrays.toString(arr)); //[3, 2, 1]
        Arrays.sort(arr);
        System.out.println(Arrays.toString(arr)); //[1, 2, 3]
    }
}
