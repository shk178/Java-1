package scanner;
import java.util.Scanner;

public class Scanner2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("정수 입력하세요: ");
        int intValue1 = scanner.nextInt();
        System.out.println("입력한 정수: " + intValue1); //1
        System.out.print("정수 입력하세요: ");
        int intValue2 = scanner.nextInt();
        System.out.println("입력한 정수: " + intValue2); //2
        System.out.println("결과: " + (intValue1 + intValue2)); //결과: 3
        System.out.println(intValue1 + intValue2); //3
        System.out.println("결과: " + intValue1 + intValue2); //결과: 12
        int result = (intValue1 > intValue2) ? intValue1 : intValue2;
        System.out.println("큰 수: " + result);
    }
}
