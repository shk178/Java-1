package except2;
import java.util.Scanner;

public class Main2 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        NetworkService2 service = new NetworkService2();
        while (true) {
            System.out.print("data 입력 (종료는 exit): ");
            String data = input.nextLine();
            if (data.equals("exit")) {
                break;
            } else {
                service.sendMessage(data);
            }
        }
    }
}
