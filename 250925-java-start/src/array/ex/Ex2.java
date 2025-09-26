package array.ex;
import java.util.Scanner;

public class Ex2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("count: ");
        int count = scanner.nextInt();
        scanner.nextLine();
        int[] numbers = new int[count];
        int sum = 0;
        double avg = 0;
        System.out.print("numbers: ");
        for (int i=0; i<count; i++) {
            int num = scanner.nextInt();
            numbers[i] = num;
            sum += num;
        }
        avg = (double) sum / count;
        System.out.println("sum = " + sum);
        System.out.println("avg = " + avg);
    }
}
