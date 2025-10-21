package ioex;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Member1 {
    public static final String FILE_PATH = "temp/member1.dat";
    public static final String DELIMITER = ",";
    String id;
    String name;
    int age;
    public Member1(String id, String name, int age) throws IOException {
        this.id = id;
        this.name = name;
        this.age = age;
        write();
    }
    private void write() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, StandardCharsets.UTF_8, true))) {
            bw.write(id + DELIMITER + name + DELIMITER + age);
            bw.newLine();
        }
    }
}
