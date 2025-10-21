package ioex;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Member {
    String id;
    String name;
    int age;
    public Member(String id, String name, int age) throws IOException {
        this.id = id;
        this.name = name;
        this.age = age;
        write();
    }
    private void write() throws IOException {
        File dir = new File("temp");
        if (!dir.exists()) dir.mkdirs();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("temp/member.dat", true))) {
            dos.writeUTF(id); // 2byte 추가 사용해서 문자열 길이 정보 저장, UTF-8로 저장
            dos.writeUTF(name);
            dos.writeInt(age);
        }
    }
}
