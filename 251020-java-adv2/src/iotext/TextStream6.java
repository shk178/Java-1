package iotext;

import java.io.*;
import static iotext.TextConst.FILE_NAME;

public class TextStream6 {
    public static void main(String[] args) throws IOException {
        String writeString = "ABC\n가나다";
        FileOutputStream fos = new FileOutputStream(FILE_NAME);
        DataOutputStream dos = new DataOutputStream(fos);
        dos.writeChars(writeString); //writeChars(): 각 문자를 2바이트 유니코드로 저장
        //"ABC\n가나다" (7글자) = 14바이트
        dos.writeInt(10); //writeInt(10): 4바이트
        //총 18바이트가 파일에 저장됨
        dos.close();
        FileInputStream fis = new FileInputStream(FILE_NAME);
        DataInputStream dis = new DataInputStream(fis);
        //System.out.println(dis.readUTF()); //UTF-8 형식으로 읽으려고 시도
        //readUTF()는 처음 2바이트를 문자열 길이로 해석
        //하지만 파일에는 UTF-8 형식이 아닌 Char 형식으로 저장되어 있음
        //형식이 맞지 않아 EOFException 발생
        //파일에서 데이터를 읽을 때는 쓴 순서대로 읽어야 한다. (파일은 순차적인 바이트 스트림)
        //읽기 - 문자를 하나씩 읽기
        for (int i = 0; i < writeString.length(); i++) {
            System.out.print(dis.readChar()); //ABC
            //가나다
        }
        System.out.println();
        System.out.println(dis.readInt()); //10
    }
}
