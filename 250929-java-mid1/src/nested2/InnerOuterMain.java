package nested2;

public class InnerOuterMain {
    public static void main(String[] args) {
        InnerOuter innerOuter = new InnerOuter();
        InnerOuter.Inner inner = innerOuter.new Inner();
        inner.innerMethod();
        System.out.println(InnerOuter.outerClassField); //13
        System.out.println(innerOuter.outerInstanceField); //23
        System.out.println(inner.innerInstanceField); //33
    }
}
