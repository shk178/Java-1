package except1;

public class NetworkClient {
    private final String address;
    public NetworkClient(String address) {
        this.address = address;
    }
    public String connect() {
        System.out.println(address + " 연결 성공");
        return "success";
    }
    public String send(String data) {
        System.out.println(address + "에 데이터 전송: " + data);
        return "success";
    }
    public void disconnect() {
        System.out.println(address + " 연결 해제");
    }
}
