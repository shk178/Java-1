package scanner;
import java.util.Scanner;

public class Scanner3 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("문자열 입력: ");
            String str = scanner.nextLine();
            if (str.equals("exit")) {
                break;
            }
        }
    }
}
