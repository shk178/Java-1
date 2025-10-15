package sync;

import static thread.MyLogger.log;

public class ex3 {
    public static void main(String[] args) throws InterruptedException {
        Immutable i = new Immutable();
        Runnable task3 = new Runnable() {
            @Override
            public void run() {
                i.setValue(10);
            }
        };
        Runnable task4 = new Runnable() {
            @Override
            public void run() {
                i.setValue(11);
            }
        };
        Thread t1 = new Thread(task3);
        Thread t2 = new Thread(task4);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log(i.getValue()); //13:30:49.226 [     main] 1
    }
    static class Immutable {
        //private final int value; //java: variable value not initialized in the default constructor
        public void setValue(int value) {
            //this.value = value; //java: cannot assign a value to final variable value
        }
        public int getValue() {
            //return value;
            return 1;
        }
    }
}
//필드에 final이 붙으면 어떤 스레드도 값을 변경할 수 없다.
//멀티스레드 상황에 문제 없는 안전한 공유 자원이 된다.