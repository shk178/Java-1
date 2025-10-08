package array1;

import java.util.Arrays;

public class arrList2 {
    private static final int DEFAULT_CAPACITY = 5;
    private Object[] elementData;
    private int size = 0;
    public arrList2() {
        elementData = new Object[DEFAULT_CAPACITY];
    }
    public arrList2(int initCapacity) {
        elementData = new Object[initCapacity];
    }
    public int size() {
        return size;
    }
    public void add(Object e) {
        if (size == elementData.length) {
            grow();
        }
        elementData[size] = e;
        size++;
    }
    public void grow() {
        Object[] temp = new Object[2*elementData.length];
        int index = 0;
        for (Object e : elementData) {
            temp[index] = e;
            index++;
        }
        elementData = temp;
    }
    public Object get(int index) {
        return elementData[index];
    }
    public Object set(int index, Object e) {
        Object temp = get(index);
        elementData[index] = e;
        return temp;
    }
    public int indexOf(Object e) {
        for (int i = 0; i < size; i++) {
            if (e.equals(elementData[i])) return i;
        }
        return -1;
    }
    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(elementData, size)) +
                " size=" + size +
                " capacity=" + elementData.length;
    }
    public static void main(String[] args) {
        arrList2 list = new arrList2();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");
        list.add("f");
    }
}
