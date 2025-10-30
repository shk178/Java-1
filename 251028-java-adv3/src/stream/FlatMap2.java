package stream;

import java.util.Arrays;
import java.util.List;

public class FlatMap2 {
    static class Student {
        String name;
        String[] courses;
        Student(String name, String[] courses) {
            this.name = name;
            this.courses = courses;
        }
    }
    public static void main(String[] args) {
        List<Student> students = Arrays.asList(
                new Student("김", new String[]{"a", "b"}),
                new Student("이", new String[]{"c", "d"}),
                new Student("박", new String[]{"e", "f"})
        );
        System.out.println(students);
        students.stream()
                .flatMap(s -> Arrays.stream(s.courses))
                .forEach(System.out::println);
    }
    //[stream.FlatMap2$Student@4e50df2e, stream.FlatMap2$Student@1d81eb93, stream.FlatMap2$Student@7291c18f]
    //a
    //b
    //c
    //d
    //e
    //f
}
/*
// Object의 기본 toString() 형식
클래스이름@해시코드
// 그래서 출력된 것:
stream.FlatMap2$Student@4e50df2e
 */