package array.ex;
import java.util.Scanner;

public class Ex3 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("count: ");
        int count = scanner.nextInt();
        scanner.nextLine();
        int[] numbers = new int[count];
        int minNumber, maxNumber;
        System.out.print("numbers: ");
        for (int i = 0; i < count; i++) {
            numbers[i] = scanner.nextInt();
        }
        minNumber = maxNumber = numbers[0];
        for (int j = 1; j < count; j++) {
            if (numbers[j] < minNumber) {
                minNumber = numbers[j];
            } else if (maxNumber < numbers[j]) {
                maxNumber = numbers[j];
            }
        }
        System.out.println("minNumber = " + minNumber);
        System.out.println("maxNumber = " + maxNumber);
    }
}
