package nested2.capture;

public class LocalCapture2 {
    public Printable process(int paramVar) {
        int localVar = 1;
        class Local implements Printable {
            int value = 2;
            public void print() {
                System.out.println(value);
                System.out.println(localVar);
                System.out.println(paramVar);
            }
        }
        Printable local = new Local();
        return local;
    }
    public void outMethod() {
        Object o = process(3);
        if (o instanceof Printable) {
            ((Printable) o).print(); //안전하게 호출 가능
        }
    }
}
