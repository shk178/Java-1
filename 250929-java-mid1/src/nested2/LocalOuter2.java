package nested2;

public class LocalOuter2 {
    public void localOuter2Method() {
        int localVar = 0;
        int finalLocalVar = localVar;
        class Local {
            Local() {
                //System.out.println(localVar); //불가
                System.out.println(finalLocalVar); //가능
            }
        }
        localVar = 1;
        Local local = new Local();
    }
}
