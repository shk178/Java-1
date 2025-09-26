package array;

public class Array3 {
    public static void main(String[] args) {
        int[] students = new int[]{90, 80, 70, 60, 50};
        for (int i = 0; i < students.length; i++) {
            System.out.println("students" + (i + 1) + ": " + students[i]);
        }
        int[] arr = {1, 2, 3};
        for (int i = 0; i < arr.length; i++) {
            System.out.println("arr[" + i + "]: " + arr[i]);
        }
    }
}
