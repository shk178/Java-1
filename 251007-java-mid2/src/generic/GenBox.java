package generic;

//클래스명은 GenBox
//클래스 종류는 제네릭 클래스 (타입을 미리 결정하지 않음)
//제네릭 클래스는 클래스명 옆에 <T>와 같이 선언한다.
//T를 타입 매개변수라고 한다.
public class GenBox<T> {
    private T value;
    public void set(T t) {
        this.value = t;
    }
    public T get() {
        return value;
    }
    public static void main(String[] args) {
        GenBox<Integer> gen1 = new GenBox<Integer>(); //생성 시점에 타입 결정
        //gen1.set("100"); //java: incompatible types: java.lang.String cannot be converted to java.lang.Integer
        gen1.set(10);
        System.out.println(gen1.get().getClass()); //class java.lang.Integer
        GenBox<Integer> gen2 = new GenBox<>(); //생성 시점에 타입 추론
    }
}
