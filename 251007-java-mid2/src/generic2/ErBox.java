package generic2;

public class ErBox<T> {
    private T t;
    ErBox (T t) {
        this.t = t;
    }
    /*
    boolean check(Object obj) {
        return obj instanceof T; //java: java.lang.Object cannot be safely cast to T
    }
     */
    //T는 제네릭 타입 파라미터인데
    //instanceof는 런타임에 타입 정보를 필요로 한다.
    //타입 소거 때문에 T의 실제 타입 정보를 런타임에 알 수 없다.
    //그래서 obj instanceof T는 컴파일 에러 난다.
    /*
    T gen() {
        return new T();
        //java: unexpected type
        //  required: class
        //  found:    type parameter T
    }
     */
    //T는 런타임에 어떤 클래스인지 알 수 없기 때문에
    //new T()로 객체를 생성할 수 없다.
    public static void main(String[] args) {
        ErBox<String> box = new ErBox<>("hello");
        //System.out.println(box.check("world"));
        //String generated = box.gen();
    }
}
