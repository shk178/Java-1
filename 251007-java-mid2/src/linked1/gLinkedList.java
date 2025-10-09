package linked1;

import list.listInterface;

public class gLinkedList<E> implements listInterface<E> {
    private gNode<E> head;
    private int size;
    public gLinkedList() {
        this.head = null;
        this.size = 0;
    }
    static class gNode<E> {
        private E item;
        private gNode<E> next;
        public gNode(E item) {
            this.item = item;
            next = null;
        }
        public E getItem() { return item; }
        public void setItem(E item) { this.item = item; }
        public gNode<E> getNext() { return next; }
        public void setNext(gNode<E> next) { this.next = next; }
    }
    public void add(E item) {
        gNode<E> x = new gNode<E>(item);
        if (head == null) {
            head = x;
        } else {
            getLast().setNext(x);
        }
        size++;
    }
    private gNode<E> getLast() {
        if (head == null) {
            return null;
        }
        gNode<E> x = head;
        while (x.getNext() != null) {
            x = x.getNext();
        }
        return x;
    }
    public void add(int index, E item) {
        if (index < 0 || index > size) {
            return;
        }
        gNode<E> x = new gNode(item);
        if (index == 0) {
            x.setNext(head);
            head = x;
        } else {
            gNode<E> prev = getNode(index - 1);
            x.setNext(prev.getNext());
            prev.setNext(x);
        }
        size++;
    }
    private gNode<E> getNode(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        gNode<E> x = head;
        for (int i = 0; i < index; i++) {
            x = x.getNext();
        } //배열에 비해 단점
        return x;
    }
    public E get(int index) {
        gNode<E> x = getNode(index);
        if (x == null) {
            return null;
        }
        return x.getItem();
    }
    public E set(int index, E item) {
        gNode<E> x = getNode(index);
        if (x == null) {
            return null;
        }
        E temp = x.getItem();
        x.setItem(item);
        return temp;
    }
    public E remove(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        E x;
        if (index == 0) {
            x = head.getItem();
            head = head.getNext();
        } else {
            gNode<E> prev = getNode(index - 1);
            gNode<E> current = prev.getNext();
            x = current.getItem();
            prev.setNext(current.getNext());
        }
        size--;
        return x;
    }
    public int size() {
        return size;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public void printAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        gNode<E> x = head;
        while (x != null) {
            sb.append(x.getItem());
            if (x.getNext() != null) {
                sb.append(" -> ");
            }
            x = x.getNext();
        }
        sb.append("]");
        System.out.println(sb.toString());
    }
    public int indexOf(E item) {
        int index = 0;
        for (gNode<E> x = head; x != null; x = x.getNext()) {
            if (item.equals(x.getItem())) return index;
            index++;
        }
        return -1;
    }
    public static void main(String[] args) {
        gLinkedList<String> list = new gLinkedList<>();
        //데이터 추가
        list.add("A");
        list.add("B");
        list.add("C");
        System.out.print("초기 리스트: ");
        list.printAll(); //[A -> B -> C]
        //중간에 삽입
        list.add(1, "X");
        System.out.print("인덱스 1에 X 삽입: ");
        list.printAll(); //[A -> X -> B -> C]
        //데이터 조회
        System.out.println("인덱스 2의 데이터: " + list.get(2)); //B
        //데이터 수정
        list.set(2, "Y");
        System.out.print("인덱스 2를 Y로 변경: ");
        list.printAll(); //[A -> X -> Y -> C]
        //데이터 삭제
        Object removed = list.remove(1);
        System.out.print("인덱스 1 삭제 (삭제된 값: " + removed + "): ");
        list.printAll(); //[A -> Y -> C]
        //크기 확인
        System.out.println("리스트 크기: " + list.size()); //3
    }
}
