package class1;

public class ClassStart4 {
    public static void main(String[] args) {
        Student student1 = new Student();
        Student student2 = new Student();
        Student[] students = {student1, student2};
        students[0].name = "a";
        students[1].name = "b";
        for (Student s : students) {
            System.out.println("s.name = " + s.name);
        }
        for (int i = 0; i < students.length; i++) {
            Student s = students[i];
            System.out.println("s.name = " + s.name);
        }
    }
}
