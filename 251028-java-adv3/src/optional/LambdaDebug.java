package optional;

import java.util.function.Supplier;

public class LambdaDebug {
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
    public void debug(Supplier<?> supplier) {
        if (isDebug) {
            print(supplier.get());
        }
    }
}
