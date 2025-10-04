package except3;

public class NetworkService3 {
    public void sendMessage(String data) {
        String address = "http://example.com";
        NetworkClient3 client = new NetworkClient3(address);
        client.initError(data);
        try {
            client.connect();
            client.send(data);
        } finally {
            client.disconnect();
        }
    }
}
