package loop.ex;

public class Ex1 {
    public static void main(String[] args) {
        int count = 1;
        while (count <= 10) {
            System.out.println("count = " + count);
            count++;
        }

        count = 1;
        for (; count <= 10; count++) {
            System.out.println("count = " + count);
        }
    }
}
