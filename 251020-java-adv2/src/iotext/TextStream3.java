package iotext;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static iotext.TextConst.FILE_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TextStream3 {
    public static void main(String[] args) throws IOException {
        String writeString = "ABC";
        FileWriter fw = new FileWriter(FILE_NAME, UTF_8);
        fw.write(writeString);
        fw.close();
        StringBuilder content = new StringBuilder();
        FileReader fr = new FileReader(FILE_NAME, UTF_8);
        int ch;
        while ((ch = fr.read()) != -1) {
            content.append((char) ch);
        }
        fr.close();
        System.out.println(content); // ABC
    }
}
