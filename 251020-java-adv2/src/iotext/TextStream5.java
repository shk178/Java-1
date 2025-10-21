package iotext;

import java.io.*;

import static iotext.TextConst.FILE_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TextStream5 {
    private static final int BUFFER_SIZE = 8 * 1024;
    public static void main(String[] args) throws IOException {
        String writeString = "ABC\n가나다";
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        PrintStream ps = new PrintStream(fos, true, "UTF-8");
        /*
        자동 flush가 되는 경우
        autoFlush를 true로 설정하면 다음 메서드를 호출할 때 자동으로 flush
        - println(...)
        - printf(...)
        - format(...)
        하지만 print(...)나 write(...)는 자동 flush가 안 됩니다.
        자동 flush가 안 되는 경우
        - BufferedWriter는 자동 flush 기능이 없습니다.
        - 직접 flush()를 호출하거나 close()해야 버퍼가 비워져요.
        - PrintStream도 autoFlush가 false일 경우엔 자동 flush가 안 됩니다.
         */
        ps.println(writeString);
        ps.close();
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