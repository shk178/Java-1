package thread;

public class HelloDaemon {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("main() start");
        DaemonThread daemonThread = new DaemonThread();
        daemonThread.setDaemon(true);
        daemonThread.start();
        Thread.sleep(10000); //10초
        System.out.println("main() end");
    }
    static class DaemonThread extends Thread {
        @Override
        public void run() {
            String s = Thread.currentThread().getName();
            System.out.println(s + ": run() start");
            try {
                Thread.sleep(5000); //5초
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(s + ": run() end");
        }
    }
}
/*
main() start
Thread-0: run() start
Thread-0: run() end
main() end
 */