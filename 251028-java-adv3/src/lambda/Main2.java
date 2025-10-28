package lambda;

public class Main2 {
    public static void hello(Procedure procedure) {
        long startNs = System.nanoTime();
        procedure.run();
        long endNs = System.nanoTime();
        System.out.println(endNs - startNs);
    }
    public static void main(String[] args) {
        Procedure helloJava = new Procedure() {
            @Override
            public void run() {
                System.out.println("hello java 익명 클래스");
            }
        };
        Procedure helloSpring = new Procedure() {
            @Override
            public void run() {
                System.out.println("hello spring 익명 클래스");
            }
        };
        hello(helloJava);
        //hello java 익명 클래스
        //214600
        hello(helloSpring);
        //hello spring 익명 클래스
        //27500
    }
}
