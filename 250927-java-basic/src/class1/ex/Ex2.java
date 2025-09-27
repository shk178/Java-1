package class1.ex;

public class Ex2 {
    public static void main(String[] args) {
        ProductOrder po1 = new ProductOrder("상품1", 1, 2);
        ProductOrder po2 = new ProductOrder("상품2", 3, 4);
        ProductOrder[] productOrders = new ProductOrder[]{po1, po2};
        for (ProductOrder po : productOrders) {
            System.out.println("productName = " + po.productName);
            System.out.println("price = " + po.price);
            System.out.println("quantity = " + po.quantity);
        }
    }
}
