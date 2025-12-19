package hello.proxy.pattern1;

public class RealSubject implements Subject {
    @Override
    public String operation() {
        System.out.println("RealSubject.operation");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "data";
    }
}
