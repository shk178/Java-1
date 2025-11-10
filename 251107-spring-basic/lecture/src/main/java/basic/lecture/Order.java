package basic.lecture;

public class Order {
    public Long id;
    public String name;
    public int price;
    public int discount;
    public Order(Long id, String name, int price, int discount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.discount = discount;
    }
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", discount=" + discount +
                '}';
    }
}
