package ref;

public class MethodChange1 {
    public static void main(String[] args) {
        Data a = new Data();
        a.value = 10;
        int b = 20;
        int[] c = {30};
        System.out.println("a.value = " + a.value);
        System.out.println("b = " + b);
        System.out.println("c[0] = " + c[0]);
        changeRef(a, b, c);
        System.out.println("a.value = " + a.value);
        System.out.println("b = " + b);
        System.out.println("c[0] = " + c[0]);
    }
    static void changeRef(Data x, int y, int[] z) {
        x.value = 100;
        y = 200;
        z[0] = 300;
    }
}
