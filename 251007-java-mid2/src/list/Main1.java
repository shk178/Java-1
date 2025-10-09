package list;

import array1.arrList4;
import linked1.gLinkedList;

public class Main1 {
    public static void main(String[] args) {
        listInterface<Integer> arr = new arrList4<>();
        listInterface<Integer> linked = new gLinkedList<>();
        BatchProcessor batch1 = new BatchProcessor(arr);
        BatchProcessor batch2 = new BatchProcessor(linked);
        batch1.logicFront(10000); //logicFront list=array1.arrList4 size=10000 time=61
        batch2.logicFront(10000); //logicFront list=linked1.gLinkedList size=10000 time=1
        batch1.logicBack(10000); //logicBack list=array1.arrList4 size=10000 time=1
        batch2.logicBack(10000); //logicBack list=linked1.gLinkedList size=10000 time=183
        batch1.setList(new gLinkedList<>());
        batch1.logicFront(10000); //logicFront list=linked1.gLinkedList size=10000 time=15
        batch1.logicBack(10000); //logicBack list=linked1.gLinkedList size=10000 time=183
    }
}
