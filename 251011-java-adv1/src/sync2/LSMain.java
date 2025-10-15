package sync2;
import java.util.concurrent.locks.LockSupport;

public class LSMain {
    static volatile boolean isParked = false;
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            log("스레드 시작");
            isParked = true; //park 직전 플래그 설정
            log("park 호출");
            LockSupport.park();
            log("park 해제됨");
            log("isInterrupted=" + Thread.currentThread().isInterrupted());
        }, "ParkThread");
        t1.start();
        //park()에 도달할 때까지 기다림
        while (!isParked) {
            Thread.yield(); //CPU 양보하며 기다림
        }
        Thread.sleep(100); //아주 짧은 여유
        log("t1.getState=" + t1.getState());
        //log("Main: unpark 호출");
        //LockSupport.unpark(t1);
        //또는 interrupt 테스트
        log("Main: interrupt 호출");
        t1.interrupt();
    }
    static void log(String msg) {
        System.out.println(Thread.currentThread().getName() + ": " + msg);
    }
}
/*
log("Main: unpark 호출");
LockSupport.unpark(t1);
-
ParkThread: 스레드 시작
ParkThread: park 호출
main: t1.getState=WAITING
main: Main: unpark 호출
ParkThread: park 해제됨
ParkThread: isInterrupted=false
 */
/*
log("Main: interrupt 호출");
t1.interrupt();
-
ParkThread: 스레드 시작
ParkThread: park 호출
main: t1.getState=WAITING
main: Main: interrupt 호출
ParkThread: park 해제됨
ParkThread: isInterrupted=true
 */