package method.ex;

public class Ex2 {
    public static void main(String[] args) {
        int balance = 10000;
        //입금 1000
        balance = deposit(balance, 1000);
        System.out.println("balance = " + balance);
        //출금 500
        balance = withdraw(balance, 500);
        System.out.println("balance = " + balance);
    }
    //메서드 추출 리팩토링
    public static int deposit(int balance, int amount) {
        balance += amount;
        System.out.println(amount + "원 입금했습니다.");
        return balance;
    }
    public static int withdraw(int balance, int amount) {
        if (balance < amount) {
            System.out.println("출금에 실패했습니다.");
        } else {
            balance -= amount;
            System.out.println(amount + "원 출금했습니다.");
        }
        return balance;
    }
}
