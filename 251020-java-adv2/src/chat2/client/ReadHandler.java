package chat2.client;

import java.io.DataInputStream;
import java.io.IOException;

import static network.MyLogger.log;

public class ReadHandler implements Runnable {
    private final DataInputStream input;
    private final Client client;
    public boolean closed = false;
    public ReadHandler(DataInputStream input, Client client) {
        this.input = input;
        this.client = client;
    }
    @Override
    public void run() {
        log("ReadHandler 실행");
        try {
            while (true) {
                String received = input.readUTF();
                System.out.println(received);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
    public synchronized void close() {
        if (closed) {
            return;
        }
        // 종료 로직 필요 시 작성
        log("ReadHandler 종료");
        closed = true;
    }
}
