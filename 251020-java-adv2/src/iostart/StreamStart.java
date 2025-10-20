package iostart;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class StreamStart {
    public static void main(String[] args) throws IOException {
        FileOutputStream fos = new FileOutputStream("temp/hello.dat"); // 파일 내용 지우고 쓰기
        // (fileName, true);로 호출하면 append가 된다. (지우지 않고 추가 쓰기)
        fos.write(65); // int b 쓰기 (파일 읽을 때 디코딩해서 표시)
        fos.write(66); // byte 단위로 값을 파일에 출력한다.
        fos.write(67);
        fos.close();
        FileInputStream fis = new FileInputStream("temp/hello.dat"); // 파일 처음부터 읽기
        System.out.println(fis.read()); // int 반환 (65) byte 단위로 파일에서 값을 읽어온다.
        System.out.println(fis.read()); // 66
        System.out.println(fis.read()); // 67
        System.out.println(fis.read()); // EOF 도달 (-1)
        System.out.println(fis.read()); // -1
        fis.close(); // 파일 내용 안 지워짐
        read(new FileInputStream("temp/hello.dat"));
    }
    private static void read(FileInputStream fis) throws IOException {
        int a;
        List<Byte> bsList = new ArrayList<>();
        while((a = fis.read()) != -1) {
            bsList.add((byte) a);
        }
        byte[] bsArr = new byte[bsList.size()];
        int i = 0;
        for (Byte b : bsList) {
            bsArr[i] = b;
            i++;
        }
        System.out.println(new String(bsArr, Charset.forName("UTF-8"))); // ABC
        fis.close();
    }
}
