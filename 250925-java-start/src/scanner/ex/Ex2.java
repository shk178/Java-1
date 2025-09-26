package scanner.ex;
import java.util.Scanner;

public class Ex2 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("음식 이름: ");
        String name = input.nextLine();
        System.out.print("음식 가격: ");
        int price = input.nextInt();
        System.out.print("음식 수량: ");
        int quantity = input.nextInt();
        System.out.println("name = " + name);
        System.out.println("totalPrice = " + (price * quantity));
    }
}
