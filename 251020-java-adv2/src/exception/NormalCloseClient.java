package exception;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class NormalCloseClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        InputStream input = socket.getInputStream();
        Random r = new Random();
        int i = r.nextInt(3);
        switch (i) {
            case 0:
                readByInputStream(socket, input);
                break;
            case 1:
                readByBufferedReader(socket, input);
                break;
            case 2:
                readByDataInputStream(socket, input);
                break;
        }
    }
    private static void readByInputStream(Socket socket, InputStream input) throws IOException {
        int read = input.read();
        if (read == -1) {
            input.close();
            socket.close();
        }
    }
    private static void readByBufferedReader(Socket socket, InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        if (line == null) {
            reader.close();
            socket.close();
        }
    }
    private static void readByDataInputStream(Socket socket, InputStream input) throws IOException {
        DataInputStream dis = new DataInputStream(input);
        try {
            dis.readUTF();
        } catch (EOFException e) {
            e.printStackTrace();
        } finally {
            dis.close();
            socket.close();
        }
    }
    //java.io.EOFException
    //	at java.base/java.io.DataInputStream.readFully(DataInputStream.java:210)
    //	at java.base/java.io.DataInputStream.readUnsignedShort(DataInputStream.java:341)
    //	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:575)
    //	at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:550)
    //	at exception.NormalCloseClient.readByDataInputStream(NormalCloseClient.java:43)
    //	at exception.NormalCloseClient.main(NormalCloseClient.java:21)
}
