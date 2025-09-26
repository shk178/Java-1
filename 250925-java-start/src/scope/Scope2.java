package scope;

public class Scope2 {
    public static void main(String[] args) {
        int sum = 0;
        int i = 1;
        int endNum = 3;
        while (i <= endNum) {
            sum = sum + i;
            System.out.println("sum = " + sum);
            i++;
        }
        sum = 0;
        for (int j = 1; j <= endNum; j++) {
            sum = sum + j;
            System.out.println("sum = " + sum);
        }
        System.out.println("i = " + i);
        //System.out.println("j = " + j);
    }
}
