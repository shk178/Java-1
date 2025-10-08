package generic2.ex;

public class Marine extends BioUnit {
    public Marine(String name, int hp) {
        super(name, hp);
    }
    public void marineOnly() {
        System.out.println("marine");
    }
}
