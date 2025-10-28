package lambda2;

import java.util.Objects;

public class Main1 {
    public static void main(String[] args) {
        SamInterface samInterface = () -> {
            System.out.println("samInterface.run");
        };
        samInterface.run(); // samInterface.run
        // 메서드 시그니처만 맞추고 매개변수명은 바꿔써도 된다.
        MyFunction myFunction = (int xx, int yy) -> {
            return xx + yy;
        };
        System.out.println(myFunction.apply(1, 2)); // 3
    }
}
