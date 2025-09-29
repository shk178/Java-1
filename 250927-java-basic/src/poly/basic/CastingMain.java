package poly.basic;

public class CastingMain {
    public static void main(String[] args) {
        Parent poly = new Child();
        poly.parentMethod();
        //poly.childMethod();
        ((Child) poly).childMethod();
        Child child = (Child) poly; //명시적 다운캐스팅
        child.parentMethod();
        child.childMethod();
        System.out.println(new Parent() instanceof Parent);
        System.out.println(new Parent() instanceof Child);
        System.out.println(new Child() instanceof Parent);
        System.out.println(new Child() instanceof Child);
    }
}
