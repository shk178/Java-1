package access.b;

public class BankAccount {
    private int balance;
    public BankAccount() {
        this.balance = 0;
    }
    public void deposit(int amount) {
        if (isAmountValid(amount)) {
            this.balance += amount;
        } else {
            System.out.println("잘못 입력");
        }
    }
    public void withdraw(int amount) {
        if (isAmountValid(amount) && (this.balance - amount >= 0)) {
            this.balance -= amount;
        } else {
            System.out.println("잘못 입력");
        }
    }
    private boolean isAmountValid(int amount) {
        return amount > 0;
    }
}
