package sync2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static control.ThreadUtils.sleep;
import static thread.MyLogger.log;

public class BankAccountL {
    private int balance;
    private final Lock lock = new ReentrantLock(true);
    public BankAccountL(int balance) {
        this.balance = balance;
    }
    public boolean deposit(int amount) {
        if(!lock.tryLock()) {
            log("입금 불가: " + amount);
            return false;
        }
        //락을 가진 상태 (다른 스레드는 balance에 접근할 수 없다.)
        try {
            balance += amount;
            log("입금 완료: " + amount);
        } finally {
            //finally에서 unlock()을 호출해야 한다.
            lock.unlock();
            //unlock()에 앞서 예외가 발생해서
            //락이 해제되지 못해서 발생하는
            //교착(deadlock) 상황을 방지하기 위해서다.
        }
        //unlock() 다음 코드는 락의 보호를 받지 않는다.
        //임계 구역 (Critical Section), 비임계 구역 (Non-critical Section)이라고 한다.
        log("거래 종료: " + amount);
        return true;
    }
    public static void main(String[] args) {
        BankAccountL account = new BankAccountL(1000);
        Runnable task = () -> {
            for (int i = 100; i < 600; i += 100) {
                account.deposit(i);
                sleep(50); //약간 쉬었다가 재시도 (락 경합 유도)
            }
        };
        //스레드 여러 개 실행
        Thread t1 = new Thread(task, "고객1");
        Thread t2 = new Thread(task, "고객2");
        Thread t3 = new Thread(task, "고객3");
        t1.start();
        t2.start();
        t3.start();
        //메인 스레드가 모두 끝날 때까지 대기
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("최종 잔액: " + account.balance);
    }
}
/*
21:50:56.310 [      고객1] 입금 완료: 100
21:50:56.310 [      고객3] 입금 불가: 100
21:50:56.310 [      고객2] 입금 불가: 100
21:50:56.312 [      고객1] 거래 종료: 100
21:50:56.377 [      고객2] 입금 불가: 200
21:50:56.377 [      고객1] 입금 완료: 200
21:50:56.377 [      고객3] 입금 불가: 200
21:50:56.377 [      고객1] 거래 종료: 200
21:50:56.431 [      고객3] 입금 완료: 300
21:50:56.431 [      고객2] 입금 불가: 300
21:50:56.431 [      고객1] 입금 불가: 300
21:50:56.431 [      고객3] 거래 종료: 300
21:50:56.494 [      고객3] 입금 완료: 400
21:50:56.494 [      고객1] 입금 불가: 400
21:50:56.494 [      고객2] 입금 불가: 400
21:50:56.494 [      고객3] 거래 종료: 400
21:50:56.545 [      고객2] 입금 불가: 500
21:50:56.545 [      고객1] 입금 완료: 500
21:50:56.545 [      고객1] 거래 종료: 500
21:50:56.547 [      고객3] 입금 완료: 500
21:50:56.547 [      고객3] 거래 종료: 500
최종 잔액: 3000
 */