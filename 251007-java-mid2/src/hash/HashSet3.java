package hash;

import java.util.ArrayList;
import java.util.LinkedList;

public class HashSet3<E> {
    private ArrayList<LinkedList<E>> buckets;
    private int capacity;
    private int size;
    HashSet3(int capacity) {
        buckets = new ArrayList<>(capacity); //초기 용량 지정해서 생성
        for (int i = 0; i < capacity; i++) {
            buckets.add(new LinkedList<>());
        }
        this.capacity = capacity;
        this.size = 0;
    }
    public int hashIndex(E e) {
        return Math.abs(e.hashCode() % capacity);
    }
    public boolean add(E e) {
        if (size != 0 && contains(e)) {
            return false;
        }
        buckets.get(hashIndex(e)).add(e);
        size++;
        return true;
    }
    public boolean contains(E e) {
        return buckets.get(hashIndex(e)).contains(e);
    }
    public boolean remove(E e) {
        if (buckets.get(hashIndex(e)).remove(e)) {
            size--;
            return true;
        }
        return false;
    }
    public int size() {
        return size;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public void printAll() {
        for (int i = 0; i < capacity; i++) {
            System.out.print("Bucket " + i);
            System.out.println(": " + buckets.get(i));
        }
    }
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            buckets.get(i).clear();
        }
        size = 0;
    }
}
