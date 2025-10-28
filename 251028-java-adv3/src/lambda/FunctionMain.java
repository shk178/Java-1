package lambda;

public class FunctionMain {
    public static void main(String[] args) {
        MyFunction myFunction = new MyFunction() {
            @Override
            public int apply(int x) {
                int y = x;
                return y;
            }
        };
        int result = myFunction.apply(1);
        System.out.println(result); // 1
        MyFunction function = (x) -> {
            int y = 2*x;
            return y;
        };
        result = function.apply(1);
        System.out.println(result); // 2
    }
}
