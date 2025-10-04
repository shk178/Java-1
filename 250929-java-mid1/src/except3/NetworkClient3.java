package except3;

public class NetworkClient3 {
    private final String address;
    public boolean connectError;
    public boolean sendError;
    public NetworkClient3(String address) {
        this.address = address;
    }
    public void connect() throws RException {
        if (connectError) {
            throw new RException(address + " 연결 오류");
        }
        System.out.println(address + " 연결");
    }
    public void send(String data) throws RException2 {
        if (sendError) {
            throw new RException2(address + "에 " + data + " 전송 오류");
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
