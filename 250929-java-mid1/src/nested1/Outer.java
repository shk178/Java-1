package nested1;

public class Outer {
    private static int staticOuterValue = 10;
    private int instanceOuterValue = 20;
    static class Inner {
        private static int staticInnerValue = 100;
        private int instanceInnerValue = 200;
        void print() {
            System.out.println(this.instanceInnerValue); //자기 인스턴스 멤버
            System.out.println(this.staticInnerValue);        //자기 static 멤버
            System.out.println(Outer.staticOuterValue);        //바깥 클래스의 static 멤버
            //System.out.println(Outer.this.instanceOuterValue);   //바깥 클래스의 인스턴스 멤버 접근 불가
        }
    }
}
