package iotext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static iotext.TextConst.FILE_NAME;

public class TextStream {
    public static void main(String[] args) throws IOException {
        String writeString = "ABC";
        byte[] writeBytes = writeString.getBytes(StandardCharsets.UTF_8);
        System.out.println(writeString); // ABC
        System.out.println(Arrays.toString(writeBytes)); // [65, 66, 67]
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        fos.write(writeBytes);
        fos.close();
        FileInputStream fis = new FileInputStream(FILE_NAME);
        byte[] readBytes = fis.readAllBytes();
        fis.close();
        String readString = new String(readBytes, StandardCharsets.UTF_8);
        System.out.println(Arrays.toString(readBytes)); // [65, 66, 67]
        System.out.println(readString); // ABC
    }
}
