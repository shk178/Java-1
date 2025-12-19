package hello.proxy.dynamic;

public class AImpl implements AInterface {
    @Override
    public String call() {
        System.out.println("AImpl.call");
        return "a";
    }
}
