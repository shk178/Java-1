package lambda4;

import java.util.List;

public class StreamMain3 {
    public static class Student {
        public String name;
        public int age;
        public Student(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
    public static void main(String[] args) {
        List<Student> students = List.of(
                new Student("Apple", 100),
                new Student("Banana", 80),
                new Student("Berry", 50),
                new Student("Tomato", 40)
        );
        MyStream3.of(students)
                .filter(s -> s.age > 40)
                .mapper(s -> s.name)
                .forEach(str -> System.out.println(str));
    }
    //Apple
    //Banana
    //Berry
}
