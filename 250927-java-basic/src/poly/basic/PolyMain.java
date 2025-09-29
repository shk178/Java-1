package poly.basic;

public class PolyMain {
    public static void main(String[] args) {
        Parent parent = new Parent();
        parent.parentMethod();
        //parent.childMethod();
        System.out.println("parent instanceof Parent: " + (parent instanceof Parent));
        System.out.println("parent instanceof Child: " + (parent instanceof Child));
        Child child = new Child();
        child.childMethod();
        child.parentMethod();
        System.out.println("child instanceof Parent: " + (child instanceof Parent));
        System.out.println("child instanceof Child: " + (child instanceof Child));
        Parent parent2 = new Child();
        parent2.parentMethod();
        //parent2.childMethod();
        System.out.println("parent2 instanceof Parent: " + (parent2 instanceof Parent));
        System.out.println("parent2 instanceof Child: " + (parent2 instanceof Child));
        //Child child2 = new Parent();
        //child2.childMethod();
        //child2.parentMethod();
        Parent p = new Child();
        System.out.println("p instanceof Parent: " + (p instanceof Parent));
        System.out.println("p instanceof Child: " + (p instanceof Child));
        Child c = (Child) p;
        c.parentMethod();
        c.childMethod();
        System.out.println("c instanceof Parent: " + (c instanceof Parent));
        System.out.println("c instanceof Child: " + (c instanceof Child));
    }
}
