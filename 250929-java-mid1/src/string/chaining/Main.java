package string.chaining;

public class Main {
    public static void main(String[] args) {
        ValueAdder adder = new ValueAdder();
        adder.add(1);
        adder.add(2);
        adder.add(3);
        System.out.println(adder.getValue());
        adder.add(4).add(5).add(6);
        System.out.println(adder.getValue());
    }
}
