package scanner.ex;
import java.util.Scanner;

public class Ex5 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("min: ");
        int min = input.nextInt();
        System.out.print("max: ");
        int max = input.nextInt();
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
            System.out.println("min = " + min);
            System.out.println("max = " + max);
        }
        for (int i = min; i <= max; i++) {
            System.out.print(i);
            if (i < max) {
                System.out.print(", ");
            }
        }
    }
}
