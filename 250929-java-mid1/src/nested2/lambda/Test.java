package nested2.lambda;
import java.util.Random;

public class Test {
    public static void hello(String s, TwoInterface t) {
        System.out.println(s + " 시작");
        t.run();
        System.out.println(s + " 종료");
    }
    public static void main(String[] args) {
        /*
        hello("Dice", () -> {
            System.out.println(new Random().nextInt(6) + 1);
        });
        hello("Sum", () -> {
            for (int i = 1; i <= 3; i++) { System.out.println(i); }
        });
        */
        //java: incompatible types: nested2.lambda.TwoInterface is not a functional interface
        //multiple non-overriding abstract methods found in interface nested2.lambda.TwoInterface
    }
}
