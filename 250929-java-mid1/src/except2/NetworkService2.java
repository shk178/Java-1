package except2;

public class NetworkService2 {
    public void sendMessage(String data) {
        String address = "http://example.com";
        NetworkClient2 client = new NetworkClient2(address);
        client.initError(data);
        try {
            client.connect();
            client.send(data);
        } catch (NException | NException2 e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            client.disconnect();
        }
    }
}
