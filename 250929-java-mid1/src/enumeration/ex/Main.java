package enumeration.ex;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("HTTP CODE: ");
        int codeValue = input.nextInt();
        HttpStatus status = HttpStatus.findByCode(codeValue);
        System.out.println("status = " + status);
    }
}
