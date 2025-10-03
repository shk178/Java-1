package nested2;

class InnerOuter {
    public static int outerClassField = 0;
    public int outerInstanceField = 1;
    class Inner {
        public int innerInstanceField = 2;
        void innerMethod() {
            int innerLocalField = 3;
            //바깥 클래스의 클래스 변수 쓰기 가능
            InnerOuter.outerClassField = 10;
            InnerOuter.this.outerClassField = 11;
            //this.outerClassField = 12;
            outerClassField = 13;
            //바깥 클래스의 인스턴스 변수 쓰기 가능
            //InnerOuter.outerInsInstanceField = 20;
            InnerOuter.this.outerInstanceField = 21;
            //this.outerInstanceField = 22;
            outerInstanceField = 23;
            //내부 클래스의 인스턴스 변수 쓰기 가능
            //InnerOuter.Inner.innerInstanceField = 30;
            InnerOuter.Inner.this.innerInstanceField = 31;
            this.innerInstanceField = 32;
            innerInstanceField = 33;
            //내부 메서드의 지역 변수 쓰기 가능
            //this.innerLocalField = 40;
            innerLocalField = 41;
            //바깥 메서드 호출만 가능
            outerMethodOne(1);
            outerMethodTwo();
        }
    }
    void outerMethodOne(int i) {
        int outerLocalVarOne = i;
    }
    void outerMethodTwo() {
        final int outerLocalVarTwo = 0;
    }
}