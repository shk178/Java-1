package files;

import java.io.FileOutputStream;
import java.io.IOException;

public class CreateFile {
    private static final int FILE_SIZE = 200 * 1024 * 1024;
    public static void main(String[] args) throws IOException {
        String fileName = "temp/copy.dat";
        long sTime = System.currentTimeMillis();
        FileOutputStream fos = new FileOutputStream(fileName);
        byte[] buffer = new byte[FILE_SIZE];
        fos.write(buffer);
        fos.close();
        long eTime = System.currentTimeMillis();
        System.out.println("(eTime - sTime) = " + (eTime - sTime)); // (eTime - sTime) = 262
    }
}
