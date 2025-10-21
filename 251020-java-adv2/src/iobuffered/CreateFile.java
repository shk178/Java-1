package iobuffered;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static iobuffered.BufferedConst.*;

public class CreateFile {
    public static void main(String[] args) throws IOException {
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        long sTime = System.currentTimeMillis();
        for (int i = 0; i < FILE_SIZE; i++) {
            fos.write(0);
        }
        fos.close();
        long eTime = System.currentTimeMillis();
        System.out.println(formatBytes(FILE_SIZE) + " file created in " + (eTime - sTime) + "ms");
        // 10.00MB file created in 23034ms
        FileInputStream fis = new FileInputStream(FILE_NAME);
        sTime = System.currentTimeMillis();
        int fileSize = 0;
        int data;
        while ((data = fis.read()) != -1) {
            fileSize++;
        }
        fis.close();
        eTime = System.currentTimeMillis();
        System.out.println(formatBytes(fileSize) + " file read in " + (eTime - sTime) + "ms");
        // 10.00MB file read in 16985ms
    }
    public static String formatBytes(int bytes) {
        final String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f%s", size, units[unitIndex]);
    }
}
