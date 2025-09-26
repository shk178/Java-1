package scanner.ex;
import java.util.Scanner;

public class Ex7 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int priceSum = 0;
        while (true) {
            System.out.print("1 주문, 2 결제, 3 종료: ");
            int option = input.nextInt();
            input.nextLine();
            if (option == 1) {
                System.out.print("상품명: ");
                String name = input.nextLine();
                System.out.print("상품 가격: ");
                int price = input.nextInt();
                System.out.print("상품 수량: ");
                int quantity = input.nextInt();
                priceSum += (price * quantity);
            } else if (option == 2) {
                System.out.println("결제 비용: " + priceSum);
                priceSum = 0;
            } else if (option == 3) {
                System.out.println("종료");
                break;
            } else {
                System.out.println("올바른 옵션 입력");
            }
        }
    }
}
