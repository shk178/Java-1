package array.ex;
import java.util.Scanner;

public class Ex1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int[] num = new int[5];
        for (int i = 0; i < num.length; i++) {
            System.out.print("num: ");
            num[i] = scanner.nextInt();
        }
        for (int j = num.length - 1; j >= 0; j--) {
            System.out.print(num[j]);
            if (j > 0) {
                System.out.print(", ");
            }
        }
    }
}
