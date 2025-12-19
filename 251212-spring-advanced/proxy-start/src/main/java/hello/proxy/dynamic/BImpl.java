package hello.proxy.dynamic;

public class BImpl implements BInterface {
    @Override
    public String call() {
        System.out.println("BImpl.call");
        return "b";
    }
}
