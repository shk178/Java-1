package iobuffered;

import java.io.*;

import static iobuffered.BufferedConst.BUFFER_SIZE;
import static iobuffered.BufferedConst.FILE_NAME;
import static iobuffered.BufferedConst.FILE_SIZE;

public class CreateFile3 {
    public static void main(String[] args) throws IOException {
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
        long sTime = System.currentTimeMillis();
        for (int i = 0; i < FILE_SIZE; i++) {
            bos.write(0); // 내부 버퍼에 1바이트씩 저장
            // 내부 버퍼가 차면 fos.write(버퍼) 실행
            /*
            bos.write(0); // 매번 1바이트씩 write() 호출이 되지만,
            // 내부적으로는 BUFFER_SIZE만큼 모아서 fos.write()를 호출
            bos.write(byte[], off, len); // 이렇게 하면 더 빠름
             */
        }
        bos.close();
        /*
        bos.close(); 실행 시
        bos.flush(); 자동 실행됨 // 남은 데이터 강제 출력, 버퍼가 안 찬 상태에서 fos.write 실행
        fos.close(); 자동 실행됨 // bos.flush() 하기 전에 fos.close() 하면 안 된다.
         */
        long eTime = System.currentTimeMillis();
        System.out.println(formatBytes(FILE_SIZE) + " file created in " + (eTime - sTime) + "ms");
        // 10.00MB file created in 70ms
        FileInputStream fis = new FileInputStream(FILE_NAME);
        BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE);
        sTime = System.currentTimeMillis();
        int fileSize = 0;
        int data;
        while ((data = bis.read()) != -1) {
            fileSize++; // 1바이트씩 읽은 횟수 카운트
        }
        /*
        1. 블록 단위로 읽기 (디스크 → 메모리)
        BufferedInputStream은 read()가 처음 호출되면
        내부적으로 FileInputStream.read(byte[])를 사용해서
        BUFFER_SIZE만큼 한 번에 디스크에서 읽어와서 내부 버퍼에 저장
        2. 1바이트씩 반환 (메모리 → 사용자 코드)
        이후 read()를 호출할 때마다 내부 버퍼에서 1바이트씩 꺼내서 반환
        디스크에 다시 접근하지 않고 메모리에서 빠르게 처리
        3. 버퍼가 다 소진되면 다시 블록 단위로 읽기
        내부 버퍼가 비면 다시 BUFFER_SIZE만큼 디스크에서 읽어와서 버퍼를 채우고
        또 1바이트씩 꺼내는 방식
         */
        bis.close();
        eTime = System.currentTimeMillis();
        System.out.println(formatBytes(fileSize) + " file read in " + (eTime - sTime) + "ms");
        // 10.00MB file read in 66ms
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
