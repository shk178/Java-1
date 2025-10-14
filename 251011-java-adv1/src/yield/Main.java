package yield;

import static control.ThreadUtils.sleep;

public class Main {
    static final int THREAD_COUNT = 5;
    public static void main(String[] args) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(new MyRunnable()).start();
        }
    }
    static class MyRunnable implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                System.out.println(Thread.currentThread().getName() + " - " + i);
                //(empty)
                //sleep(1);
                Thread.yield();
            }
        }
    }
}
/*
//1. empty
Thread-0 - 0
Thread-0 - 1
Thread-4 - 0
Thread-4 - 1
Thread-1 - 0
Thread-2 - 0
Thread-2 - 1
Thread-3 - 0
Thread-0 - 2
Thread-4 - 2
Thread-1 - 1
Thread-2 - 2
Thread-3 - 1
Thread-1 - 2
Thread-3 - 2
 */
/*
//sleep(1);
Thread-4 - 0
Thread-0 - 0
Thread-3 - 0
Thread-2 - 0
Thread-1 - 0
Thread-2 - 1
Thread-4 - 1
Thread-3 - 1
Thread-1 - 1
Thread-0 - 1
Thread-2 - 2
Thread-4 - 2
Thread-0 - 2
Thread-3 - 2
Thread-1 - 2
 */
/*
//Thread.yield();
Thread-0 - 0
Thread-0 - 1
Thread-0 - 2
Thread-1 - 0
Thread-3 - 0
Thread-2 - 0
Thread-1 - 1
Thread-3 - 1
Thread-2 - 1
Thread-1 - 2
Thread-4 - 0
Thread-3 - 2
Thread-2 - 2
Thread-4 - 1
Thread-4 - 2
 */