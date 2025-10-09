package hash;

import java.util.Arrays;

public class hashSet {
    private int[] elementData = new int[10];
    private int size = 0;
    //O(n)
    public boolean add(int value) {
        if (contains(value)) {
            return false;
        }
        if(size == elementData.length) {
            grow();
        }
        elementData[size] = value;
        size++;
        return true;
    }
    //O(n)
    public boolean contains(int value) {
        for (int data : elementData) {
            if (data == value) {
                return true;
            }
        }
        return false;
    }
    public void grow() {
        int[] temp = new int[(int) (elementData.length * 1.5)];
        System.arraycopy(elementData, 0, temp, 0, elementData.length);
        elementData = temp;
    }
    public int size() {
        return size;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    @Override
    public String toString() {
        return "hashSet{" +
                "elementData=" + Arrays.toString(Arrays.copyOf(elementData, size)) +
                ", size=" + size +
                '}';
    }
}
