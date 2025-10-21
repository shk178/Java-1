package iotext;

import java.io.*;

import static iotext.TextConst.FILE_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TextStream4 {
    private static final int BUFFER_SIZE = 8 * 1024;
    public static void main(String[] args) throws IOException {
        String writeString = "ABC\n가나다";
        FileWriter fw = new FileWriter(FILE_NAME, UTF_8);
        BufferedWriter bw = new BufferedWriter(fw, BUFFER_SIZE);
        bw.write(writeString);
        bw.close();
        StringBuilder content = new StringBuilder();
        FileReader fr = new FileReader(FILE_NAME, UTF_8);
        BufferedReader br = new BufferedReader(fr, BUFFER_SIZE);
        String line;
        while ((line = br.readLine()) != null) {
            content.append(line).append("\n");
        }
        br.close();
        System.out.println(content); // ABC
        // 가나다
    }
}
