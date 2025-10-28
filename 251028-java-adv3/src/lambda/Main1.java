package lambda;

public class Main1 {
    public static void hello(Procedure procedure) {
        long startNs = System.nanoTime();
        procedure.run();
        long endNs = System.nanoTime();
        System.out.println(endNs - startNs);
    }
    static class helloJava implements Procedure {
        @Override
        public void run() {
            System.out.println("hello java");
        }
    }
    static class helloSpring implements Procedure {
        @Override
        public void run() {
            System.out.println("hello spring");
        }
    }
    public static void main(String[] args) {
        hello(new helloJava());
        //hello java
        //312900
        hello(new helloSpring());
        //hello spring
        //99199
    }
}
