package sync;

import static thread.MyLogger.log;

public class BankAccount2 implements BankAccount {
    private int balance;
    public BankAccount2(int balance) {
        this.balance = balance;
    }
    //방법 1: synchronized 메서드
    @Override
    public synchronized boolean withdraw(int amount) {
        int result = balance - amount;
        log("출금 검증(r=b-a): r=" + result + ", b=" + balance + ", a=" + amount);
        if (result < 0) {
            log("출금 불가(r<0): r=" + result + ", b=" + balance + ", a=" + amount);
            return false;
        }
        balance = result;
        log("출금 완료(r>=0): r=" + result + ", b=" + balance + ", a=" + amount);
        return true;
    }
    @Override
    public int getBalance() {
        return balance;
    }
}
