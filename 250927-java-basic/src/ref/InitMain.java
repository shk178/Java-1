package ref;

public class InitMain {
    public static void main(String[] args) {
        InitData data = new InitData();
        System.out.println("data.value1 = " + data.value1);
        System.out.println("data.value2 = " + data.value2);
        Data x = null;
        System.out.println("x = " + x);
        //System.out.println("x.value = " + x.value);
        x = new Data();
        System.out.println("x = " + x);
        System.out.println("x.value = " + x.value);
        x = null;
        System.out.println("x = " + x);
        //System.out.println("x.value = " + x.value);
    }
}
