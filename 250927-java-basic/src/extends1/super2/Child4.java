package extends1.super2;

public class Child4 extends Parent4 {
    public int init = 0;
    public String value = "child";
    @Override
    public void hello() {
        System.out.println("child hello");
    }
    public void call() {
        System.out.println("this.value = " + this.value);
        System.out.println("super.value = " + super.value);
        super.value = "PARENT";
        System.out.println("this.value = " + this.value);
        System.out.println("super.value = " + super.value);
        hello();
        super.hello();
    }
    /*
    Child4(int i) {
        System.out.println("Child 생성");
    }
     */
}
