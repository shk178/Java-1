//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        int[] arr = new int[3];
        //index 입력: O(1)
        arr[0] = 1;
        arr[1] = 2;
        arr[2] = 3;
        //index 변경: O(1)
        arr[2] = 10;
        //index 조회: O(1)
        System.out.println(arr[2]); //10
        //arr 검색: O(n)
        int value = 10;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) break;
        }
        add(arr, 1, 0);
    }
    private static void add(int[] arr, int i, int value) {
        for (int e : arr) {
            System.out.print(e + ", ");
        } // 1, 2, 10,
        System.out.println();
        //arr[arr.length] = arr[arr.length - 1];
        //Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
        //	at Main.add(Main.java:26)
        //	at Main.main(Main.java:19)
        for (int j = arr.length - 1; j > i; j--) {
            arr[j] = arr[j - 1];
        }
        arr[i] = value;
        for (int e : arr) {
            System.out.print(e + ", ");
        } //1, 0, 2,
    }
}