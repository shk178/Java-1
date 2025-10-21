package ioex;

import java.io.*;

public class Member2 implements Serializable {
    public static final String FILE_PATH = "temp/member2.dat";
    String id;
    String name;
    int age;
    public Member2(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    @Override
    public String toString() {
        return "id=" + id + ", name=" + name + ", age=" + age;
    }
}
