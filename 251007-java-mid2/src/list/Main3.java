package list;

import array1.arrList4;
import linked1.gLinkedList;
import java.util.ArrayList;
import java.util.LinkedList;

public class Main3 {
    public static void main(String[] args) {
        BatchProcessor batch1 = new BatchProcessor(new arrList4<>());
        BatchProcessor batch2 = new BatchProcessor(new gLinkedList<>());
        BatchProcessor2 batch3 = new BatchProcessor2(new ArrayList<>());
        BatchProcessor2 batch4 = new BatchProcessor2(new LinkedList<>());
        batch1.logicFront(10000);
        batch2.logicFront(10000);
        batch3.logicFront(10000);
        batch4.logicFront(10000);
        System.out.println("---");
        batch1.logicMiddle(10000);
        batch2.logicMiddle(10000);
        batch3.logicMiddle(10000);
        batch4.logicMiddle(10000);
        System.out.println("---");
        batch1.logicBack(10000);
        batch2.logicBack(10000);
        batch3.logicBack(10000);
        batch4.logicBack(10000);
        System.out.println("---");
        batch1.logicIndex(10000);
        batch2.logicIndex(10000);
        batch3.logicIndex(10000);
        batch4.logicIndex(10000);
        System.out.println("---");
        batch1.logicGet(10000);
        batch2.logicGet(10000);
        batch3.logicGet(10000);
        batch4.logicGet(10000);
        System.out.println("---");
        batch1.logicSet(10000);
        batch2.logicSet(10000);
        batch3.logicSet(10000);
        batch4.logicSet(10000);
    }
}
/*
logicFront list=array1.arrList4 size=10000 time=64
logicFront list=linked1.gLinkedList size=10000 time=1
logicFront list=java.util.ArrayList size=10000 time=0
logicFront list=java.util.LinkedList size=10000 time=0
---
logicMiddle list=array1.arrList4 size=10000 time=107
logicMiddle list=linked1.gLinkedList size=10000 time=102
logicMiddle list=java.util.ArrayList size=10000 time=6
logicMiddle list=java.util.LinkedList size=10000 time=97
---
logicBack list=array1.arrList4 size=10000 time=4
logicBack list=linked1.gLinkedList size=10000 time=321
logicBack list=java.util.ArrayList size=10000 time=0
logicBack list=java.util.LinkedList size=10000 time=0
---
logicIndex list=array1.arrList4 size=10000 time=50
logicIndex list=linked1.gLinkedList size=10000 time=92
logicIndex list=java.util.ArrayList size=10000 time=32
logicIndex list=java.util.LinkedList size=10000 time=96
---
logicGet list=array1.arrList4 size=10000 time=0
logicGet list=linked1.gLinkedList size=10000 time=61
logicGet list=java.util.ArrayList size=10000 time=0
logicGet list=java.util.LinkedList size=10000 time=78
---
logicSet list=array1.arrList4 size=10000 time=1
logicSet list=linked1.gLinkedList size=10000 time=59
logicSet list=java.util.ArrayList size=10000 time=0
logicSet list=java.util.LinkedList size=10000 time=65
 */