package poly.pay;

public class DefaultPay implements Pay {
    @Override
    public boolean pay(int amount){
        System.out.println("연결x");
        return false;
    }
}
