package stream;

public class Main {
    public static void main(String[] args) {
        MyOptional<Integer> optional1 = MyOptional.of(10);
        System.out.println(optional1);
        System.out.println(optional1.get());
        MyOptional<Integer> optional2 = MyOptional.of(null);
        System.out.println(optional2);
        try {
            System.out.println(optional2.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
stream.MyOptional@2f4d3709
10
stream.MyOptional@4e50df2e
java.util.NoSuchElementException: value == null
	at stream.MyOptional.get(MyOptional.java:18)
	at stream.Main.main(Main.java:11)
 */