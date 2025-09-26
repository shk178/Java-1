package method;

public class Method6 {
    public static void main(String[] args) {
        int num1 = 5;
        System.out.println("num1 = " + num1);
        num1 = changeNumber(num1); //바뀐다.
        System.out.println("num1 = " + num1);
    }
    public static int changeNumber(int num2) {
        System.out.println("num2 = " + num2);
        num2 *= 2;
        System.out.println("num2 = " + num2);
        return num2;
    }
}
