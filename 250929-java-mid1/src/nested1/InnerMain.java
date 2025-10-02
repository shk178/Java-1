package nested1;

public class InnerMain {
    public static void main(String[] args) {
        //InnerOuter.innerClass.innerClassMethod();
        //InnerOuter.innerClass nested = new InnerOuter.innerClass();
        //nested.innerInstanceMethod();
        //inner class는 외부 클래스의 인스턴스 없이 직접 생성할 수 없다.
        InnerOuter outer = new InnerOuter(); //외부 클래스 인스턴스 생성
        InnerOuter.innerClass nested = outer.new innerClass(); //inner 클래스 인스턴스 생성
        nested.innerInstanceMethod(); //메서드 호출
    }
}
