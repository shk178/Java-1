package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static network.MyLogger.log;

public class Server1 {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        log("서버 시작");
        ServerSocket server = new ServerSocket(PORT);
        Socket socket = server.accept();
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        DataInputStream input = new DataInputStream(socket.getInputStream());
        String request = input.readUTF();
        output.writeUTF(request + " World");
        output.close();
        input.close();
        socket.close();
        server.close();
        log("서버 종료");
    }
}
