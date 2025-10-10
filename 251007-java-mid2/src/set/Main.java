package set;

public class Main {
    public static void main(String[] args) {
        MySet<String> set = new HashSet4<>(10);
        set.add("a");
        set.add("a");
        //set.add(1); //java: incompatible types: int cannot be converted to java.lang.String
        System.out.println(set);
        //HashSet4{buckets=[[], [], [], [], [], [], [], [a], [], []], capacity=10, size=1}
    }
}
