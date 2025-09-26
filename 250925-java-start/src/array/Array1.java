package array;

public class Array1 {
    public static void main(String[] args) {
        int[] students; //int 배열 변수 선언
        students = new int[5]; //배열 생성
        students[0] = 90;
        students[1] = 80;
        students[2] = 70;
        students[3] = 60;
        students[4] = 50;
        System.out.println("students[0] = " + students[0]);
        System.out.println("students[1] = " + students[1]);
        System.out.println("students[2] = " + students[2]);
        System.out.println("students[3] = " + students[3]);
        System.out.println("students[4] = " + students[4]);
        System.out.println(students);
    }
}
