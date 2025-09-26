package loop.ex;

public class Ex2 {
    public static void main(String[] args) {
        int num = 2;
        int count = 1;
        while (count <= 10) {
            System.out.println("num = " + num);
            num = num + 2;
            count++;
        }
        num = 2;
        count = 1;
        for (; count <= 10; count++) {
            System.out.println("num = " + num);
            num += 2;
        }
    }
}
