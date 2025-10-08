package array1;

import java.util.Arrays;

public class arrList3 {
    private static final int DEFAULT_CAPACITY = 5;
    private Object[] elementData;
    private int size = 0;
    public arrList3() {
        elementData = new Object[DEFAULT_CAPACITY];
    }
    public arrList3(int initCapacity) {
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
    public void add(int index, Object e) {
        if (size == elementData.length) {
            grow();
        }
        shiftRightFrom(index);
        elementData[index] = e;
        size++;
        //shiftRightFrom 루프 후 임시로 elementData[size]에 데이터가 있어도
        //add에서 size++를 해야만 데이터가 공식적으로 리스트의 일부가 된다.
    }
    private void shiftRightFrom(int index) {
        for (int i = size; i > index; i--) {
            elementData[i] = elementData[i - 1];
        }
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
    public Object remove(int index) {
        Object temp = elementData[index];
        shiftLeftFrom(index);
        size--;
        elementData[size] = null;
        return temp;
    }
    private void shiftLeftFrom(int index) {
        for (int i = index; i < size - 1; i++) {
            elementData[i] = elementData[i + 1];
        }
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
        arrList3 list = new arrList3();
        list.add("a");
        list.add("b");
        list.add("c");
        list.remove(0);
        list.remove(0);
        list.remove(0);
        //list.remove(0);
        //Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 5
        //	at array1.arrList3.remove(arrList3.java:53)
        //	at array1.arrList3.main(arrList3.java:89)
    }
}
