package hello.proxy.pattern1;

public class CacheProxy implements Subject {
    private Subject target;
    private String cacheValue;
    public CacheProxy(Subject target) {
        this.target = target;
    }
    @Override
    public String operation() {
        System.out.println("CacheProxy.operation");
        if (cacheValue == null) {
            cacheValue = target.operation();
        }
        return cacheValue;
    }
}
