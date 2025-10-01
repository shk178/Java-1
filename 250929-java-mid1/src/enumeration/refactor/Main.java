package enumeration.refactor;

public class Main {
    public static void main(String[] args) {
        int price = 10000;
        Grade2[] grade2s = Grade2.values();
        for (Grade2 grade2 : grade2s) {
            print(grade2, price);
        }
    }
    private static void print(Grade2 grade2, int price) {
        System.out.println(grade2 + " " + grade2.discountWon(price));
    }
}
//BASIC 1000
//GOLD 2000
//DIA 3000