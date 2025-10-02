package nested1;
//innerClass는 non-static inner class이므로
//outer 클래스의 모든 멤버에 접근 가능
//하지만 static 멤버를 가질 수 없음 (Java 21에서도 여전히 금지)
//static 메서드도 정의할 수 없음. → 컴파일 에러 발생
public class InnerOuter {
    private static int outerClassValue = 10;
    private int outerInstanceValue = 20;
    class innerClass {
        //private static int innerClassValue = 100; //불가능: inner class는 static 멤버를 가질 수 없음
        private int innerInstanceValue = 200;
        //public static void innerClassMethod() { //불가능: inner class는 static 메서드를 가질 수 없음
            //System.out.println(outerClassValue);
            ////System.out.println(outerInstanceValue);
            //System.out.println(innerClassValue);
            ////System.out.println(innerInstanceValue);
            ////innerInstanceMethod();
        //}
        public void innerInstanceMethod() { //인스턴스 메서드
            System.out.println(outerClassValue); //가능
            System.out.println(outerInstanceValue); //가능
            //System.out.println(innerClassValue); //static 멤버를 가질 수 없음 접근 불가
            System.out.println(innerInstanceValue); //가능
            //innerClassMethod(); //static 메서드가 정의 불가하므로 호출도 불가
        }
    }
    private static void outerClassMethod() {
        //innerClassMethod(); //innerClass는 인스턴스 클래스이므로 직접 호출 불가
        //innerInstanceMethod(); //인스턴스 생성 없이 호출 불가
        //innerClass.innerClassMethod(); //innerClass는 인스턴스 클래스이므로 static 접근 불가
        //innerClass.innerInstanceMethod(); //인스턴스 생성 없이 호출 불가
    }
    private void outerInstanceMethod() {
        //innerClassMethod(); //innerClass는 인스턴스 클래스이므로 직접 호출 불가
        //innerInstanceMethod(); //인스턴스 생성 없이 호출 불가
        //innerClass.innerClassMethod(); //innerClass는 인스턴스 클래스이므로 static 접근 불가
        //innerClass.innerInstanceMethod(); //인스턴스 생성 없이 호출 불가
    }
}
