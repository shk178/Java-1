package nested2.anon;
import java.util.Random;

public class After {
    public static void hello(String s, AfterInterface a) {
        System.out.println(s + " 시작");
        a.run();
        System.out.println(s + " 종료");
    }
    static class Dice implements AfterInterface {
        @Override
        public void run() {
            System.out.println(new Random().nextInt(6) + 1);
        }
    }
    static class Sum implements AfterInterface {
        @Override
        public void run() {
            for (int i=1; i<=3; i++) {
                System.out.println(i);
            }
        }
    }
    public static void main(String[] args) {
        hello("Dice", new Dice());
        hello("Sum", new Sum());
    }
}
