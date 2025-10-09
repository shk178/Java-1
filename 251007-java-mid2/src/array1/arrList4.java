package array1;

import linked1.gLinkedList;
import list.listInterface;
import java.util.Arrays;

public class arrList4<E> implements listInterface<E> {
    private static final int DEFAULT_CAPACITY = 5;
    private Object[] elementData;
    private int size = 0;
    public arrList4() {
        elementData = new Object[DEFAULT_CAPACITY];
    }
    public arrList4(int initCapacity) {
        elementData = new Object[initCapacity];
    }
    public int size() {
        return size;
    }
    public void add(E e) {
        if (size == elementData.length) {
            grow();
        }
        elementData[size] = e;
        size++;
    }
    public void add(int index, E e) {
        if (size == elementData.length) {
            grow();
        }
        shiftRightFrom(index);
        elementData[index] = e;
        size++;
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
    public E remove(int index) {
        E temp = get(index);
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
    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) elementData[index];
    }
    public E set(int index, E e) {
        E temp = get(index);
        elementData[index] = e;
        return temp;
    }
    public int indexOf(E e) {
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
    public void printAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
            sb.append(get(i));
            if (i != size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        System.out.println(sb.toString());
    }
    public static void main(String[] args) {
        arrList4<String> list = new arrList4<>();
        list.add("a");
        list.add("b");
        list.add("c");
        System.out.println(list.toString()); //[a, b, c] size=3 capacity=5
        list.add(1, "e");
        System.out.println(list.toString()); //[a, e, b, c] size=4 capacity=5
        list.remove(1);
        System.out.println(list.toString()); //[a, b, c] size=3 capacity=5
    }
}
