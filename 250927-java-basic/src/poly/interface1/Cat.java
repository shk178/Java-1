package poly.interface1;

class Cat implements MyInterface {
    @Override
    public void sound() {
        System.out.println("냥");
    }
    @Override
    public void move() {
        System.out.println("냥냥");
    }
}
