package poly.pay;

public class KakaoPay implements Pay {
    @Override
    public boolean pay(int amount) {
        System.out.println("카카오페이 연결");
        System.out.println("amount = " + amount);
        return true;
    }
}
