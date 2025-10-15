package sync;

import static thread.MyLogger.log;

public class BankMain {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount1(1000);
        Thread t1 = new Thread(new WithdrawTask(account, 500), "t-1");
        Thread t2 = new Thread(new WithdrawTask(account, 500), "t-2");
        t1.start();
        //t1.join(); //메인 스레드가 자식 스레드의 완료를 기다리게 함
        t2.start();
        //t2.join(); //메인 스레드가 자식 스레드의 완료를 기다리게 함
        log("b=" + account.getBalance());
    }
}
/*
//주석o 실행
09:55:12.693 [      t-1] 출금 검증(r=b-a): r=500, b=1000, a=500
09:55:12.693 [     main] b=1000
09:55:12.696 [      t-1] 출금 완료(r>=0): r=500, b=500, a=500
09:55:12.693 [      t-2] 출금 검증(r=b-a): r=500, b=1000, a=500
09:55:12.696 [      t-2] 출금 완료(r>=0): r=500, b=500, a=500
//주석x 실행
09:55:57.885 [      t-1] 출금 검증(r=b-a): r=500, b=1000, a=500
09:55:57.887 [      t-1] 출금 완료(r>=0): r=500, b=500, a=500
09:55:57.888 [      t-2] 출금 검증(r=b-a): r=0, b=500, a=500
09:55:57.888 [      t-2] 출금 완료(r>=0): r=0, b=0, a=500
09:55:57.891 [     main] b=0
 */