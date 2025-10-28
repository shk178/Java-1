package lambda;

public class Main3 {
    public static void hello(Procedure procedure) {
        long startNs = System.nanoTime();
        procedure.run();
        long endNs = System.nanoTime();
        System.out.println(endNs - startNs);
    }
    public static void main(String[] args) {
        hello(new Procedure() {
            @Override
            public void run() {
                System.out.println("hello java");
            }
        });
        //hello java
        //201401
        hello(new Procedure() {
            @Override
            public void run() {
                System.out.println("hello spring");
            }
        });
        //hello spring
        //76600
    }
}
