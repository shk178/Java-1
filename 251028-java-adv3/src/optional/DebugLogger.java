package optional;

public class DebugLogger {
    private boolean isDebug = false;
    public boolean isDebug() {
        return isDebug;
    }
    public void setDebug(boolean flag) {
        isDebug = flag;
    }
    public void debug(Object msg) {
        if (isDebug) {
            print(msg);
        }
    }
    public void print(Object msg) {
        System.out.println("[DEBUG] " + msg);
    }
}
