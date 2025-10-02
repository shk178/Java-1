package nested1;
//staticNestedClass는 static nested class이므로
//outer 클래스의 static 멤버만 접근 가능
//static 메서드는 인스턴스 멤버에 접근 불가
//인스턴스 메서드는 자기 클래스의 인스턴스 멤버에는 접근 가능하지만
//outer 클래스의 인스턴스 멤버에는 접근 불가
public class StaticNestedOuter {
    private static int outerClassValue = 10;
    private int outerInstanceValue = 20;
    static class staticNestedClass {
        private static int innerClassValue = 100;
        private int innerInstanceValue = 200;
        public static void innerClassMethod() { //static 메서드
            System.out.println(outerClassValue); //가능: outer 클래스의 static 멤버
            //System.out.println(outerInstanceValue); //불가능: outer 클래스의 인스턴스 멤버
            System.out.println(innerClassValue); //가능: 자기 클래스의 static 멤버
            //System.out.println(innerInstanceValue); //불가능: static 메서드에서 인스턴스 멤버 접근 불가
            //innerInstanceMethod(); //불가능: static 메서드에서 인스턴스 메서드 호출 불가
        }
        public void innerInstanceMethod() { //인스턴스 메서드
            System.out.println(outerClassValue); //가능: outer 클래스의 static 멤버
            //System.out.println(outerInstanceValue); //불가능: outer 클래스의 인스턴스 멤버는 접근 불가
            System.out.println(innerClassValue); //가능: 자기 클래스의 static 멤버
            System.out.println(innerInstanceValue); //가능: 자기 클래스의 인스턴스 멤버
            innerClassMethod(); //가능: static 메서드 호출은 가능
        }
    }
    private static void outerClassMethod() { //static 메서드
        //innerClassMethod(); //불가능: staticNestedClass의 메서드는 직접 호출 불가
        //innerInstanceMethod(); //불가능: 인스턴스 생성 없이 인스턴스 메서드 호출 불가
        staticNestedClass.innerClassMethod(); //가능: static 메서드는 클래스명으로 호출 가능
        //staticNestedClass.innerInstanceMethod(); //불가능: 인스턴스 생성 없이 인스턴스 메서드 호출 불가
    }
    private void outerInstanceMethod() { //인스턴스 메서드
        //innerClassMethod(); //불가능: staticNestedClass의 메서드는 직접 호출 불가
        //innerInstanceMethod(); //불가능: 인스턴스 생성 없이 인스턴스 메서드 호출 불가
        staticNestedClass.innerClassMethod(); //가능: static 메서드는 클래스명으로 호출 가능
        //staticNestedClass.innerInstanceMethod(); //불가능: 인스턴스 생성 없이 인스턴스 메서드 호출 불가
    }
}
