package nested2;

public class LocalOuter {
    public void localOuterMethod() {
        int localVar = 0;
        class Local {
            Local() {
                //localVar = 2; //컴파일 에러
                System.out.println(localVar); //가능
            }
        }
        Local local = new Local();
    }
}
