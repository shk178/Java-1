package chat.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static network.MyLogger.log;

public class WriteHandler implements Runnable {
    private static final String DELIMITER = "|";
    private final DataOutputStream output;
    private final Client client;
    public boolean closed = false;
    public WriteHandler(DataOutputStream output, Client client) {
        this.output = output;
        this.client = client;
    }
    @Override
    public void run() {
        log("WriteHandler 실행");
        Scanner scanner = new Scanner(System.in);
        try {
            String username = inputUsername(scanner);
            output.writeUTF("/join" + DELIMITER + username);
            while (true) {
                String toSend = scanner.nextLine();
                if (toSend.isEmpty()) {
                    continue;
                }
                if (toSend.equals("/exit")) {
                    output.writeUTF(toSend);
                    break;
                }
                if (toSend.startsWith("/")) {
                    output.writeUTF(toSend);
                } else {
                    output.writeUTF("/message" + DELIMITER + toSend);
                }
            }
        } catch (IOException | NoSuchElementException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
    private static String inputUsername(Scanner scanner) {
        System.out.print("이름을 입력하세요: ");
        String username;
        do {
            username = scanner.nextLine();
        } while (username.isEmpty());
        return username;
    }
    public synchronized void close() {
        if (closed) {
            return;
        }
        // 종료 로직 필요 시 작성
        log("WriteHandler 종료");
        try {
            System.in.close(); // Scanner 입력 중지
        } catch (IOException e) {
            e.printStackTrace();
            // System.in.close() 했다고 IOException 발생하지 않는다.
            // NoSuchElementException이 클라이언트에서 발생한다.
        }
        closed = true;
    }
}
