package scanner;
import java.util.Scanner;

public class Scanner4 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int sum = 0;
        while (true) {
            System.out.print("정수 입력: ");
            int intValue = scanner.nextInt();
            if (intValue == 0) {
                break;
            }
            sum += intValue;
            System.out.println("sum = " + sum);
        }
    }
}
