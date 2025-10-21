package files;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class File6Main {
    public static void main(String[] args) throws IOException {
        long sTime = System.currentTimeMillis();
        FileInputStream fis = new FileInputStream("temp/copy.dat");
        FileOutputStream fos = new FileOutputStream("temp/copy_new.dat");
        byte[] bytes = fis.readAllBytes();
        fos.write(bytes);
        fos.close();
        fis.close();
        long eTime = System.currentTimeMillis();
        System.out.println("(eTime - sTime) = " + (eTime - sTime)); // (eTime - sTime) = 346
    }
}
