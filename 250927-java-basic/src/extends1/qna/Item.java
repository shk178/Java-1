package extends1.qna;

public class Item {
    public String name;
    public int price;
    Item(String name, int price) {
        this.name = name;
        this.price = price;
    }
    public void print() {
        System.out.println("name = " + name);
        System.out.println("price = " + price);
    }
}
