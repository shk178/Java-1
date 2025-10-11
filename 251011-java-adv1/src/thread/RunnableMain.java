package thread;

public class RunnableMain {
    public static void main(String[] args) {
        MyLogger.log("main() start");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                MyLogger.log("run()-0");
            }
        });
        t.start();
        Thread t2 = new Thread(() -> MyLogger.log("run()-1"));
        t2.start();
        MyLogger.log("main() end");
    }
}
//21:20:14.619 [     main] main() start
//21:20:14.623 [ Thread-0] run()-0
//21:20:14.623 [     main] main() end
//21:20:14.623 [ Thread-1] run()-1
//main()이 먼저 끝났다고 해서 다른 스레드들이 종료된 건 아니다.
