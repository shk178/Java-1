package poly.pay;

public class PayMain {
    public static void main(String[] args) {
        PayService payService = new PayService();
        int amount = 1000;
        String payOption1 = "kakao";
        payService.processPay(payOption1, amount);
        String payOption2 = "naver";
        payService.processPay(payOption2, amount);
        String payOption3 = "etc";
        payService.processPay(payOption3, amount);
    }
}
