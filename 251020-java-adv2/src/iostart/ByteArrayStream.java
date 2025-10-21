package iostart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ByteArrayStream {
    public static void main(String[] args) throws IOException {
        byte[] input = {65, 66, 67};
        // 메모리상에 데이터를 저장하는 출력 스트림
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(input);
        // bos.toByteArray()는 ByteArrayOutputStream에 저장된 데이터를 byte[]로 반환
        // bis는 이 바이트 배열을 읽는 입력 스트림
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        byte[] bytes = bis.readAllBytes();
        System.out.println(Arrays.toString(bytes)); // [65, 66, 67]
        bos.close();
        bis.close();
    }
}
// ByteArrayOutputStream은 파일처럼 동작하는 메모리 기반의 출력 스트림
// 실제 파일을 쓰는 대신, 메모리 안의 바이트 배열에 데이터를 저장
// FileOutputStream은 디스크에 저장, ByteArrayOutputStream은 메모리에 저장
// 파일 없이도 스트림 기반 로직을 테스트하거나 처리 가능