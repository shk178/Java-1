package method;

public class Method5 {
    public static void main(String[] args) {
        int num1 = 5;
        System.out.println("num1 = " + num1);
        changeNumber(num1); //안 바뀐다.
        System.out.println("num1 = " + num1);
    }
    public static void changeNumber(int num1) {
        System.out.println("num2 = " + num1);
        num1 *= 2;
        System.out.println("num2 = " + num1);
        return;
    }
}
