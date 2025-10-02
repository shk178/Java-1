package nested1.ex;

public class Network {
    public void sendMsg(String msg) {
        NetworkMsg m = new NetworkMsg(msg);
        m.sendCheck();
    }
    private static class NetworkMsg {
        private String content;
        private NetworkMsg(String content) {
            this.content = content;
        }
        private void sendCheck() {
            System.out.println(content);
        }
    }
}
