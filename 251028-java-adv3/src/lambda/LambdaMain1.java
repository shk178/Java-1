package lambda;

public class LambdaMain1 {
    public static void hello(Procedure procedure) {
        long startNs = System.nanoTime();
        procedure.run();
        long endNs = System.nanoTime();
        System.out.println(endNs - startNs);
    }
    public static void main(String[] args) {
        hello(() -> {
            System.out.println("hello java");
        });
        //hello java
        //194201
        hello(() -> {
            System.out.println("hello spring");
        });
        //hello spring
        //39700
        Procedure procedure = () -> { System.out.println("hello lambda"); };
        hello(procedure);
        //hello lambda
        //29600
    }
}