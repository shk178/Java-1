package annotation;

public class User {
    @NotEmpty(message = "유저 이름이 비어 있음")
    private String name;
    @Range(min = 1, max = 100, message = "유저 나이는 1~100 사이")
    private int age;
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
}
