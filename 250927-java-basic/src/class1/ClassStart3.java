package class1;

public class ClassStart3 {
    public static void main(String[] args) {
        Student student1 = new Student();
        Student student2 = new Student();
        Student[] students = new Student[2];
        students[0] = student1;
        students[1] = student2;
        System.out.println("student1 = " + student1);
        System.out.println("students[0] = " + students[0]);
        System.out.println("students = " + students);
        System.out.println("name = " + student1.name);
        System.out.println("name = " + students[0].name);
        //System.out.println("name = " + 0x2f4d3709.name);
        //System.out.println("name = " + 0x7291c18f[0].name);
    }
}
