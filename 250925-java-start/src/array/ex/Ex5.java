package array.ex;
import java.util.Scanner;

public class Ex5 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] productNames = new String[10]; //상품 이름
        int[] productPrices = new int[10]; //상품 가격
        int productCount = 0; //현재 등록된 상품 개수 (최대 10개 등록)
        while (true) {
            System.out.print("1. 상품 등록 | 2. 상품 목록 | 3. 종료: ");
            int option = scanner.nextInt();
            scanner.nextLine();
            if (option == 1) {
                System.out.print("상품 이름: ");
                productNames[productCount] = scanner.nextLine();
                System.out.print("상품 가격: ");
                productPrices[productCount] = scanner.nextInt();
                productCount++;
                if (productCount == 10) {
                    break;
                }
            } else if (option == 2) {
                System.out.println("상품 개수: " + productCount);
                for (int i = 0; i < productCount; i++) {
                    System.out.print(productNames[i] + ": ");
                    System.out.print(productPrices[i] + "원");
                    System.out.println();
                }
            } else if (option == 3) {
                break;
            } else {
                System.out.println("잘못 입력");
            }
        }
    }
}
