package ref.ex;
import java.util.Scanner;

public class ProductOrderMain {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("몇 개 주문: ");
        int n = input.nextInt();
        input.nextLine();
        ProductOrder[] orders = new ProductOrder[n];
        for (int i = 0; i < n; i++) {
            System.out.print("이름: ");
            String a = input.nextLine();
            System.out.print("가격: ");
            int b = input.nextInt();
            System.out.print("수량: ");
            int c = input.nextInt();
            input.nextLine();
            orders[i] = createOrder(a, b, c);
        }
        printOrder(orders);
        for (ProductOrder o : orders) {
            o.name = "1";
        }
        printOrder(orders);
    }
    static ProductOrder createOrder(String x, int y, int z) {
        ProductOrder po = new ProductOrder();
        po.name = x;
        po.price = y;
        po.count = z;
        return po;
    }
    static void printOrder(ProductOrder[] pos) {
        int sum = 0;
        for (ProductOrder po : pos) {
            System.out.println("po.name = " + po.name);
            System.out.println("po.price = " + po.price);
            System.out.println("po.count = " + po.count);
            sum += po.price * po.count;
        }
        System.out.println("sum = " + sum);
    }
}
