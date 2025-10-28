package lambda2;

public class Main3 {
    public static void main(String[] args) {
        MyFunction add = getOperation("+");
        MyFunction sub = getOperation("-");
        MyFunction etc = getOperation("etc");
        System.out.println(add.apply(1, 2)); // 3
        System.out.println(sub.apply(1, 2)); // -1
        System.out.println(etc.apply(1, 2)); // 0
        runMul((a, b) -> a * b); // 2
    }
    static void runMul(MyFunction function) {
        System.out.println(function.apply(1, 2));
    }
    static MyFunction getOperation(String operator) {
        switch(operator) {
            case "+":
                return (a, b) -> a + b;
            case "-":
                return (a, b) -> a - b;
            default:
                return (a, b) -> 0;
        }
    }
}
