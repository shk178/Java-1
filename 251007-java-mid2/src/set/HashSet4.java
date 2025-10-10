package set;

import java.util.Arrays;
import java.util.LinkedList;

public class HashSet4<E> implements MySet<E> {
    private LinkedList<E>[] buckets;
    private int capacity;
    private int size;
    public HashSet4(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        buckets = new LinkedList[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new LinkedList<>();
        }
    }
    public int hashIndex(E e) {
        return Math.abs(e.hashCode() % capacity);
    }
    @Override
    public boolean add(E e) {
        if (contains(e)) {
            return false;
        }
        buckets[hashIndex(e)].add(e);
        size++;
        return true;
    }
    @Override
    public boolean remove(E e) {
        if (buckets[hashIndex(e)].remove(e)) {
            size--;
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean contains(E e) {
        return buckets[hashIndex(e)].contains(e);
    }
    @Override
    public String toString() {
        return "HashSet4{" +
                "buckets=" + Arrays.toString(buckets) +
                ", capacity=" + capacity +
                ", size=" + size +
                '}';
    }
}
