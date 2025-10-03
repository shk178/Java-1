package nested2.capture;

public class LocalCapture {
    private int outVar = 0;
    public Object process(int paramVar) {
        int localVar = 1;
        class Local {
            int value = 2;
            public void print() {
                System.out.println(value);
                System.out.println(localVar);
                System.out.println(paramVar);
                System.out.println(outVar);
            }
        }
        Local local = new Local();
        return local;
    }
    public void outMethod() {
        //Local local = new Local(); //Local에 접근 불가
        Object o = process(3); //가능
        //o.print(); //Object라서 안 됨
    }
}
