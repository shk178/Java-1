package iotext;

import java.io.*;
import static iotext.TextConst.FILE_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TextStream2 {
    public static void main(String[] args) throws IOException {
        String writeString = "ABC";
        System.out.println(writeString); // ABC
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8);
        osw.write(writeString);
        osw.close();
        FileInputStream fis = new FileInputStream(FILE_NAME);
        InputStreamReader isr = new InputStreamReader(fis, UTF_8);
        StringBuilder content = new StringBuilder();
        int ch;
        while ((ch = isr.read()) != -1) {
            content.append((char) ch);
        }
        isr.close();
        System.out.println(content.toString()); // ABC
    }
}
