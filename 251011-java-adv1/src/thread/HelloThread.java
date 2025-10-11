package thread;

public class HelloThread extends Thread {
    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=");
        sb.append(Thread.currentThread().getName());
        sb.append(", id=");
        sb.append(Thread.currentThread().threadId());
        System.out.println(sb.toString());
    }
    public static void main(String[] args) {
        HelloThread t1 = new HelloThread();
        HelloThread t2 = new HelloThread();
        t1.run();
        t2.run();
        t1.run();
        t2.run();
        t1.start();
        t2.start();
        t1.run();
        t2.run();
        t1.run();
        t2.run();
        //t1.start(); //두 번 start() 안 됨
        //Exception in thread "main" java.lang.IllegalThreadStateException
        //	at java.base/java.lang.Thread.start(Thread.java:1525)
        //	at thread.HelloThread.main(HelloThread.java:24)
        //t2.start();
    }
}
/*
t1.start();, t2.start(); 위치에 관계 없이 같은 출력
name=main, id=1
name=main, id=1
name=main, id=1
name=main, id=1
name=main, id=1
name=main, id=1
name=main, id=1
name=main, id=1
name=Thread-1, id=23
name=Thread-0, id=22
 */