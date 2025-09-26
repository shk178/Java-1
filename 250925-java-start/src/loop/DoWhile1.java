package loop;

public class DoWhile1 {
    public static void main(String[] args) {
        int sum = 0;
        int i = 1;
        int diff = 2;
        int loop = 3;
        int endNum = i + diff * (loop - 1);
        do {
            sum = sum + i;
            System.out.println("i = " + i + ", " + "sum = " + sum);
            i = i + diff;
        } while (i <= endNum);
    }
}
