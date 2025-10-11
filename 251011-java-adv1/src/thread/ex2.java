package thread;

public class ex2 {
    public static void main(String[] args) throws InterruptedException {
        Thread a = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    MyLogger.log("A");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, "Thread-A");
        Thread b = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    MyLogger.log("B");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, "Thread-B");
        a.setDaemon(true);
        b.setDaemon(true);
        a.start();
        b.start();
        Thread.sleep(2000);
    }
}
/*
21:31:37.964 [ Thread-A] A
21:31:37.964 [ Thread-B] B
21:31:38.478 [ Thread-B] B
21:31:38.975 [ Thread-A] A
21:31:38.991 [ Thread-B] B
21:31:39.504 [ Thread-B] B
 */