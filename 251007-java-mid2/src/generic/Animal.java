package generic;

public abstract class Animal {
    private String name;
    private int size;
    protected Animal(String name, int size) {
        this.name = name;
        this.size = size;
    } //자식 클래스가 super 호출 가능하도록 protected
    public String getName() {
        return this.name;
    }
    public int getSize() {
        return this.size;
    }
    public void sound() {
        System.out.println("동물 울음 소리");
    }
    @Override
    public String toString() {
        return "Animal{" +
                "name='" + name + '\'' +
                ", size=" + size +
                '}';
    }
}
