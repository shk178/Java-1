package cond;

public class If4 {
    public static void main(String[] args) {
        int price = 10000;
        int age = 10;
        int discount = 0;
        if (price >= 10000) {
            discount = discount + 1000;
        }
        if (age <= 10) {
            discount = discount + 1000;
        }
        System.out.println("discount = " + discount);

        if (true) System.out.println("if문에서 실행됨 1"); System.out.println("if문에서 실행됨 2");
        if (false)
            System.out.println("if문에서 실행됨");
            System.out.println("if문 밖에서 실행됨");
    }
}
