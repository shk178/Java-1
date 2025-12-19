package hello.proxy.app.v1;

public class OrderRepository1Impl implements OrderRepository1 {
    @Override
    public void save(String itemId) {
        if (itemId.equals("re")) {
            throw new RuntimeException("re");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
