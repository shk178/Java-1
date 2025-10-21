package iobuffered;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static iobuffered.BufferedConst.*;

public class CreateFile2 {
    public static void main(String[] args) throws IOException {
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        long sTime = System.currentTimeMillis();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferIndex = 0;
        for (int i = 0; i < FILE_SIZE; i++) {
            buffer[bufferIndex] = 0;
            bufferIndex++;
            if (bufferIndex == BUFFER_SIZE) {
                fos.write(buffer);
                bufferIndex = 0;
            }
        }
        if (bufferIndex > 0) {
            // 버퍼에 남은 부분 쓰기
            fos.write(buffer, 0, bufferIndex);
        }
        fos.close();
        long eTime = System.currentTimeMillis();
        System.out.println(formatBytes(FILE_SIZE) + " file created in " + (eTime - sTime) + "ms");
        // 10.00MB file created in 23ms
        FileInputStream fis = new FileInputStream(FILE_NAME);
        sTime = System.currentTimeMillis();
        byte[] buffer2 = new byte[BUFFER_SIZE];
        int fileSize = 0;
        int size;
        while ((size = fis.read(buffer2)) != -1) {
            fileSize += size;
        }
        fis.close();
        eTime = System.currentTimeMillis();
        System.out.println(formatBytes(fileSize) + " file read in " + (eTime - sTime) + "ms");
        // 10.00MB file read in 5ms
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
