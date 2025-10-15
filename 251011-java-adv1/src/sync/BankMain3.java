package sync;

import static thread.MyLogger.log;

public class BankMain3 {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount3(1000);
        Thread t1 = new Thread(new WithdrawTask(account, 500), "t-1");
        Thread t2 = new Thread(new WithdrawTask(account, 500), "t-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log("b=" + account.getBalance());
    }
}
/*
13:08:16.913 [      t-1] 출금 완료(r>=0): r=500, b=1000, a=500
13:08:16.913 [      t-2] 출금 완료(r>=0): r=0, b=500, a=500
13:08:16.918 [     main] b=0
 */