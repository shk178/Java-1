package immutable.add;

public class Main {
    public static void main(String[] args) {
        ImmutableAdd obj = new ImmutableAdd(10);
        System.out.println(obj.add(20).toString());
        System.out.println(obj.toString());
    }
}
