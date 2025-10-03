package nested2.anon;
import java.util.Random;

public class Anon {
    public static void hello(String s, AfterInterface a) {
        System.out.println(s + " 시작");
        a.run();
        System.out.println(s + " 종료");
    }
    public static void main(String[] args) {
        hello("Dice", new AfterInterface() {
            @Override
            public void run() {
                System.out.println(new Random().nextInt(6) + 1);
            }
        });
        hello("Sum", new AfterInterface() {
            @Override
            public void run() {
                for (int i = 1; i <= 3; i++) {
                    System.out.println(i);
                }
            }
        });
    }
}
