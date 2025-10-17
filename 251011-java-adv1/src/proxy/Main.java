package proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> list = Collections.synchronizedList(new ArrayList<>());
        list.add("data1");
        list.add("data2");
        list.add("data3");
        System.out.println(list.getClass()); //class java.util.Collections$SynchronizedRandomAccessList
        System.out.println("list=" + list); //list=[data1, data2, data3]
    }
}
