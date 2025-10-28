package lambda2;

import java.util.Arrays;

public class ex2 {
    public static void main(String[] args) {
        SamInterface one = () -> {
            int N = 100;
            long sum = 0;
            for (int i = 1; i <= N; i++) {
                sum += i;
            }
            System.out.println("sum=" + sum);
        };
        SamInterface two = () -> {
            int[] arr = { 4, 3, 2, 1 };
            System.out.println("원본 배열: " + Arrays.toString(arr));
            Arrays.sort(arr);
            System.out.println("배열 정렬: " + Arrays.toString(arr));
        };
        measure(one); // sum=5050
        measure(two);
        //원본 배열: [4, 3, 2, 1]
        //배열 정렬: [1, 2, 3, 4]
    }
    static void measure(SamInterface samInterface) {
        long sTime = System.currentTimeMillis();
        samInterface.run();
        long eTime = System.currentTimeMillis();
    }
}
