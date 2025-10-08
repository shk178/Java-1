package array1;
import java.util.Arrays;

public class arrList {
    private static final int DEFAULT_CAPACITY = 5;
    private Object[] elementData;
    private int size = 0;
    public arrList() {
        elementData = new Object[DEFAULT_CAPACITY];
    }
    public arrList(int initCapacity) {
        elementData = new Object[initCapacity];
    }
    public int size() {
        return size;
    }
    public void add(Object e) {
        elementData[size] = e;
        size++;
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
        arrList list = new arrList();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");
        //list.add("f");
        //Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 5
        //	at array1.arrList.add(arrList.java:18)
        //	at array1.arrList.main(arrList.java:48)
    }
}
