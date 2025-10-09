package hash;

import java.util.Arrays;
import java.util.LinkedList;

public class hashSet2 {
    static final int CAPACITY = 10;
    private LinkedList<Integer>[] list; //LinkedList<Integer> 객체들을 담는 배열
    private int size; //전체 요소 개수
    static int hashIndex(int value) {
        return Math.abs(value % CAPACITY); //절댓값
    }
    //자바의 %는 수학적 나머지 아니고
    //왼쪽 % 오른쪽 양수로 계산한 후 왼쪽의 부호 유지
    hashSet2() {
        //noinspection unchecked
        this.list = (LinkedList<Integer>[]) new LinkedList[CAPACITY];
        for (int i = 0; i < CAPACITY; i++) {
            list[i] = new LinkedList<>();
        }
        this.size = 0;
    }
    public boolean add(int value) {
        if (size != 0 && contains(value)) {
            return false;
        }
        list[hashIndex(value)].add(value);
        size++;
        return true;
    }
    public boolean contains(int value) {
        return list[hashIndex(value)].contains(value);
    }
    public boolean isEmpty() {
        return size == 0;
    }
    @Override
    public String toString() {
        return "hashSet2{" +
                "list=" + Arrays.toString(list) +
                '}';
    }
    public static void main(String[] args) {
        hashSet2 set = new hashSet2();
        set.add(0);
        set.add(1);
        set.add(8);
        set.add(9);
        set.add(10);
        set.add(11);
        System.out.println(set);
    }
}
