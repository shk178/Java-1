package lambda;

public class InstanceMain {
    public static void main(String[] args) {
        Procedure one = new Procedure() {
            @Override
            public void run() {
                System.out.println("one.run");
            }
        };
        Procedure two = () -> {
            System.out.println("two.run");
        };
        System.out.println("one.getClass() = " + one.getClass());
        System.out.println("two.getClass() = " + two.getClass());
        System.out.println("one = " + one);
        System.out.println("two = " + two);
        //one.getClass() = class lambda.InstanceMain$1
        //two.getClass() = class lambda.InstanceMain$$Lambda/0x000001273e003948
        //one = lambda.InstanceMain$1@34a245ab
        //two = lambda.InstanceMain$$Lambda/0x000001273e003948@7cc355be
    }
}
