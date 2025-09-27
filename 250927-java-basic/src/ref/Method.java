package ref;

public class Method {
    public static void main(String[] args) {
        Student s = new Student();
        initStudent(s, "학샘1", 1, 2);
        printStudent(s);
    }
    static void initStudent(Student student, String name, int age, int grade) {
        student.name = name;
        student.age = age;
        student.grade = grade;
    }
    static void printStudent(Student student) {
        System.out.println("name = " + student.name);
        System.out.println("age = " + student.age);
        System.out.println("grade = " + student.grade);
    }
}
