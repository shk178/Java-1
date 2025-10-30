package lambda5;

import java.util.function.BinaryOperator;

public class MethodReference {
    public static void main(String[] args) {
        BinaryOperator<Integer> add1 = MethodReference::add;
        BinaryOperator<Integer> add2 = MethodReference::add;
        Integer result1 = add1.apply(1, 2);
        Integer result2 = add2.apply(1, 2);
    }
    static int add(int x, int y) {
        return x + y;
    }
}
