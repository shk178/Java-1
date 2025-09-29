package poly.pay;

public class NaverPay implements Pay {
    @Override
    public boolean pay(int amount) {
        System.out.println("네이버페이 연결");
        System.out.println("amount = " + amount);
        return true;
    }
}
