package nested2.anon;
import java.util.Random;

public class Before {
    public static void helloDice() {
        System.out.println("Dice 시작");
        //코드 조각 시작
        int randomValue = new Random().nextInt(6) + 1;
        System.out.println(randomValue);
        //코드 조각 종료
        System.out.println("Dice 종료");
    }
    public static void helloSum() {
        System.out.println("Sum 시작");
        //코드 조각 시작
        for (int i=1; i<=3; i++) {
            System.out.println(i);
        }
        //코드 조각 종료
        System.out.println("Sum 종료");
    }
    public static void main(String[] args) {
        helloDice();
        helloSum();
    }
}
