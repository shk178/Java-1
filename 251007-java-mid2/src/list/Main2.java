package list;

import array1.arrList4;
import linked1.gLinkedList;

public class Main2 {
    public static void main(String[] args) {
        BatchProcessor batch1 = new BatchProcessor(new arrList4<>());
        BatchProcessor batch2 = new BatchProcessor(new gLinkedList<>());
        batch1.logicFront(10000);
        batch2.logicFront(10000);
        batch1.logicMiddle(10000);
        batch2.logicMiddle(10000);
        batch1.logicBack(10000);
        batch2.logicBack(10000);
        batch1.logicIndex(10000);
        batch2.logicIndex(10000);
        batch1.logicGet(10000);
        batch2.logicGet(10000);
        batch1.logicSet(10000);
        batch2.logicSet(10000);
    }
}
/*
logicFront list=array1.arrList4 size=10000 time=57
logicFront list=linked1.gLinkedList size=10000 time=0
logicMiddle list=array1.arrList4 size=10000 time=108
logicMiddle list=linked1.gLinkedList size=10000 time=103
logicBack list=array1.arrList4 size=10000 time=2
logicBack list=linked1.gLinkedList size=10000 time=318
logicIndex list=array1.arrList4 size=10000 time=35
logicIndex list=linked1.gLinkedList size=10000 time=99
logicGet list=array1.arrList4 size=10000 time=1
logicGet list=linked1.gLinkedList size=10000 time=58
logicSet list=array1.arrList4 size=10000 time=0
logicSet list=linked1.gLinkedList size=10000 time=61
 */
