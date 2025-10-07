package generic;

public class RawType {
    public static void main(String[] args) {
        GenBox gen3 = new GenBox(); //Raw Type - Object로 추론
        GenBox<Integer> gen4 = new GenBox<>(); //권장되는 방식
        gen3.set(10);
        System.out.println(gen3.get().getClass()); //class java.lang.Integer
        //Integer i = gen3.get(); //java: incompatible types: java.lang.Object cannot be converted to java.lang.Integer
    }
}
