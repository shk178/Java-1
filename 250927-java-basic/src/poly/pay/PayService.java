package poly.pay;

public class PayService {
    public void processPay(String option, int amount) {
        Pay pay = setPay(option);
        if (pay.pay(amount)) {
            System.out.println("결제 성공");
        } else {
            System.out.println("결제x");
        }
    }
    //변하는 부분
    public Pay setPay(String option) {
        if (option.equals("kakao")) {
            return new KakaoPay();
        } else if (option.equals("naver")) {
            return new NaverPay();
        } else {
            return new DefaultPay();
        }
    }
}
