package nested2.lambda;
import java.util.Random;

public class Lambda {
    public static void hello(String s, OneInterface o) {
        System.out.println(s + " 시작");
        o.run();
        System.out.println(s + " 종료");
    }
    public static void main(String[] args) {
        hello("Dice", () -> {
            System.out.println(new Random().nextInt(6) + 1);
        });
        hello("Sum", () -> {
            for (int i = 1; i <= 3; i++) { System.out.println(i); }
        });
    }
}