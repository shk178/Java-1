package hello.proxy.app.v2;

public class OrderRepository2 {
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
