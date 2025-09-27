package oop1.ex;
import java.util.Scanner;

public class AccountMain {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Account a = new Account();
        System.out.print("balance: ");
        a.balance = input.nextInt();
        System.out.print("1. 입금 2. 출금 etc. 잔고 확인: ");
        int menu = input.nextInt();
        if (menu == 1) {
            System.out.print("금액 입력: ");
            int amount = input.nextInt();
            a.deposit(amount);
        } else if (menu == 2) {
            System.out.print("금액 입력: ");
            int amount = input.nextInt();
            a.withdraw(amount);
        } else {
            a.print();
        }
    }
}
