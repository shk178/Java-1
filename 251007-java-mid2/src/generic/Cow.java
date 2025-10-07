package generic;

public class Cow extends Animal {
    public Cow(String name, int size) {
        super(name, size);
    }
    @Override
    public void sound() {
        System.out.println("메");
    }
}
