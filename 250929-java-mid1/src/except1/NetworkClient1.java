package except1;

public class NetworkClient1 {
    private final String address;
    public boolean connectError;
    public boolean sendError;
    public NetworkClient1(String address) {
        this.address = address;
    }
    public String connect() {
        if (connectError) {
            System.out.println(address + " 연결 오류");
            return "connectError";
        }
        System.out.println(address + " 연결");
        return "success";
    }
    public String send(String data) {
        if (sendError) {
            System.out.println(address + "에 " + data + " 전송 오류");
            return "sendError";
        }
        System.out.println(address + "에 " + data + " 전송");
        return "success";
    }
    public void disconnect() {
        System.out.println(address + " 연결 해제");
    }
    public void initError(String data) {
        if (data.contains("error1")) {
            connectError = true;
        }
        if (data.contains("error2")) {
            sendError = true;
        }
    }
}
