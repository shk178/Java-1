package except1;

public class NetworkService1 {
    public void sendMessage(String data) {
        String address = "http://example.com";
        NetworkClient1 client = new NetworkClient1(address);
        client.initError(data);
        String result1 = client.connect();
        if (isError(result1)) {
            System.out.println("error1: " + result1);
        } else {
            String result2 = client.send(data);
            if (isError(result2)) {
                System.out.println("error2: " + result2);
            }
        }
        client.disconnect();
    }
    private static boolean isError(String result) {
        return !result.equals("success");
    }
}
