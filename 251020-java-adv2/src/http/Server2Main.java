package http;

import java.io.IOException;

public class Server2Main {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        HttpServer2 server = new HttpServer2(PORT);
        server.start();
    }
}
