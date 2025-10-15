package sync;

import static thread.MyLogger.log;

public class BankMain2 {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount2(1000);
        Thread t1 = new Thread(new WithdrawTask(account, 500), "t-1");
        Thread t2 = new Thread(new WithdrawTask(account, 500), "t-2");
        t1.start();
        t2.start();
        log("b=" + account.getBalance());
    }
}
/*
//방법 1: synchronized 메서드
10:09:20.515 [      t-1] 출금 검증(r=b-a): r=500, b=1000, a=500
10:09:20.515 [     main] b=1000
10:09:20.517 [      t-1] 출금 완료(r>=0): r=500, b=500, a=500
10:09:20.518 [      t-2] 출금 검증(r=b-a): r=0, b=500, a=500
10:09:20.518 [      t-2] 출금 완료(r>=0): r=0, b=0, a=500
 */