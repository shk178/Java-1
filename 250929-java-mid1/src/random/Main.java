package random;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Random random = new Random();
        System.out.println(random.nextInt()); //1078820306, -814770099 등
        System.out.println(random.nextDouble()); //0.0d ~ 1.0d
        System.out.println(random.nextBoolean()); //true or false
        System.out.println(random.nextInt(10)); //0 ~ 9
        System.out.println(random.nextInt(10) + 1); //1 ~ 10
        Random r1 = new Random(10);
        Random r2 = new Random(10);
        System.out.println("r1.nextDouble() = " + r1.nextDouble());
        //r1.nextDouble() = 0.7304302967434272 (같은 Java 버전 내에서 항상)
        System.out.println("r2.nextDouble() = " + r2.nextDouble());
        //r2.nextDouble() = 0.7304302967434272 (같은 Java 버전 내에서 항상)
    }
}
