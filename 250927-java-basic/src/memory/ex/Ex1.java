package memory.ex;
import static memory.ex.MathArrayUtils.*;

public class Ex1 {
    public static void main(String[] args) {
        int[] array = {6, 7, 8, 2, 3, 4};
        System.out.println("sum(array) = " + sum(array));
        System.out.println("avg(array) = " + avg(array));
        System.out.println("min(array) = " + min(array));
        System.out.println("max(array) = " + max(array));
        //MathArrayUtils mathArrayUtils = new MathArrayUtils();
    }
}
