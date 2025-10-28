package http6;

import java.io.Serializable;

public class Member implements Serializable {
    public static final String FILE_PATH = "temp/member8.dat";
    String id;
    String name;
    int age;
    public Member(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
    @Override
    public String toString() {
        return "id=" + id + ", name=" + name + ", age=" + age;
    }
}
