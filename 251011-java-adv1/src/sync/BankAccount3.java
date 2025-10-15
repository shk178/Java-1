package sync;

import static thread.MyLogger.log;

public class BankAccount3 implements BankAccount {
    private int balance;
    public BankAccount3(int balance) {
        this.balance = balance;
    }
    //방법 2: Synchronized Blocks
    @Override
    public boolean withdraw(int amount) {
        int r;
        int b;
        //최소한의 임계 영역만 보호
        synchronized (this) {
            b = balance;
            r = b - amount;
            if (r < 0) {
                log("출금 불가(r<0): r=" + r + ", b=" + b + ", a=" + amount);
                return false;
            }
            balance = r; //balance 쓰기는 반드시 synchronized 안에
        }
        //성공 로그는 밖에서
        log("출금 완료(r>=0): r=" + r + ", b=" + b + ", a=" + amount);
        return true;
    }
    @Override
    public int getBalance() {
        synchronized (this) {
            return balance;
        }
    }
}