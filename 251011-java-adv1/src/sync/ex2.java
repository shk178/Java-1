package sync;

import static thread.MyLogger.log;

public class ex2 {
    public static void main(String[] args) {
        Counter2 counter2 = new Counter2();
        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                counter2.count();
            }
        };
        Thread t1 = new Thread(task2);
        Thread t2 = new Thread(task2);
        t1.start();
        t2.start();
    }
    static class Counter2 {
        public void count() {
            int localValue = 0;
            for (int i = 0; i < 10000; i++) {
                localValue += i;
            }
            log(localValue);
        }
    }
}
/*
//스택 영역은 스레드마다 가지는 별도의 메모리 공간
//지역 변수는 스택 영역에 생성
//지역 변수는 다른 스레드와 공유되지 않는다.
13:21:10.085 [ Thread-1] 49995000
13:21:10.085 [ Thread-0] 49995000
 */