package ref;

public class VarChange2 {
    public static void main(String[] args) {
        Data a = new Data();
        Data b = a;
        a.value = 10;
        System.out.println("a.value = " + a.value);
        System.out.println("b.value = " + b.value);
        b.value = 20;
        System.out.println("a.value = " + a.value);
        System.out.println("b.value = " + b.value);
        System.out.println("a = " + a);
        System.out.println("b = " + b);
    }
}
