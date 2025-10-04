package except2;

public class NetworkClient2 {
    private final String address;
    public boolean connectError;
    public boolean sendError;
    public NetworkClient2(String address) {
        this.address = address;
    }
    public void connect() throws NException {
        if (connectError) {
            throw new NException(address + " 연결 오류");
        }
        System.out.println(address + " 연결");
    }
    public void send(String data) throws NException2 {
        if (sendError) {
            throw new NException2(address + "에 " + data + " 전송 오류");
        }
        System.out.println(address + "에 " + data + " 전송");
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
