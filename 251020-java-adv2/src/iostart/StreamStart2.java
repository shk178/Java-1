package iostart;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class StreamStart2 {
    public static void main(String[] args) throws IOException {
        FileOutputStream fos = new FileOutputStream("temp/hello2.dat");
        byte[] input = {65, 66, 67}; // ASCII 값: A, B, C
        fos.write(input); // 파일에 A, B, C 저장
        fos.close();
        FileInputStream fis = new FileInputStream("temp/hello2.dat");
        byte[] buffer = new byte[10]; // 10바이트 크기의 버퍼
        int readCount = fis.read(buffer, 0, 10); // 최대 10바이트 읽기
        // buffer: 바이트 배열 (바이트 단위로 쓰거나 읽어서 int 배열은 안 된다.)
        // off: 오프셋 - 데이터를 저장할 시작 위치가 byteArr[off]
        // len: 최대 읽을 바이트 수 - 최대 len바이트 읽기 시도
        System.out.println(readCount); // 3
        System.out.println(Arrays.toString(buffer)); // [65, 66, 67, 0, 0, 0, 0, 0, 0, 0]
        // 파일의 끝(EOF)에 도달
        // reset()은 InputStream 클래스에 정의되어 있지만
        // FileInputStream은 이를 오버라이드하지 않아서 지원하지 않음
        // BufferedInputStream 같은 일부 스트림은 내부 버퍼를 사용해서 mark()와 reset()을 지원함
        fis.close(); // 리소스 해제 (파일 핸들 닫기) - 여러 스트림이면 각각 해제 필요
        // 객체를 close()했다고 해서 즉시 메모리에서 정리되지 않음
        // close()는 리소스를 해제하는 역할
        // 객체의 메모리 정리(Garbage Collection)는 JVM이 따로 관리
        fis = new FileInputStream("temp/hello2.dat");
        // fis는 참조 변수 (stack에 저장됨)
        // 이전 객체는 더 이상 참조되지 않아서 GC 대상이 됨
        byte[] buffer2 = fis.readAllBytes();
        System.out.println(Arrays.toString(buffer2)); // [65, 66, 67]
        fis.close();
    }
}
