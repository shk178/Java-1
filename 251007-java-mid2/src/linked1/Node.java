package linked1;

class Node {
    private Object item; //저장 데이터
    private Node next; //다음 노드 참조
    public Node(Object item) {
        this.item = item;
        this.next = null;
    }
    public Object getItem() {
        return item;
    }
    public void setItem(Object item) {
        this.item = item;
    }
    public Node getNext() {
        return next;
    }
    public void setNext(Node next) {
        this.next = next;
    }
}