package except3;

import java.util.Scanner;

public class Main3 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        NetworkService3 service = new NetworkService3();
        while (true) {
            System.out.print("data 입력 (종료는 exit): ");
            String data = input.nextLine();
            if (data.equals("exit")) {
                break;
            } else {
                try {
                    service.sendMessage(data);
                } catch (Exception e) {
                    exceptionHandler(e);
                }
            }
        }
    }
    public static void exceptionHandler(Exception e) {
        System.out.println("사용자 메시지");
        e.printStackTrace(System.out); //스택 트레이스 출력
        if (e instanceof RException | e instanceof RException2) {
            System.out.println("필요 시 추가 처리");
        }
    }
}
