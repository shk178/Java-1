package class1;

public class ClassStart2 {
    public static void main(String[] args) {
        Student student1 = new Student();
        Student student2 = new Student();
        Student[] students = new Student[3];
        students[0] = student1;
        students[1] = student2;
        for (Student student : students) {
            //System.out.println("name = " + student.name);
            //System.out.println("age = " + student.age);
            //System.out.println("grade = " + student.grade);
        }
        System.out.println("students[2] = " + students[2]);
    }
}
