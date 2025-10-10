package msq;
import java.util.*;

public class Cart {
    private Map<Product, Integer> map = new HashMap<>();
    public void add(Product product, int count) {
        map.put(product, map.getOrDefault(product, 0) + count);
    }
    public void minus(Product product, int count) {
        Integer integer = map.getOrDefault(product, 0);
        if (integer - count <= 0) {
            map.remove(product);
        } else {
            map.put(product, map.get(product) - count);
        }
    }
    public void printAll() {
        for (Map.Entry<Product, Integer> e : map.entrySet()) {
            System.out.print(e.getKey().getName() + " ");
            System.out.print(e.getKey().getPrice() + " ");
            System.out.println(e.getValue());
        }
        System.out.println();
    }
}
