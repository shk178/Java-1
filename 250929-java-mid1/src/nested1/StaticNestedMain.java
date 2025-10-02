package nested1;

public class StaticNestedMain {
    public static void main(String[] args) {
        StaticNestedOuter.staticNestedClass.innerClassMethod();
        StaticNestedOuter.staticNestedClass nested = new StaticNestedOuter.staticNestedClass(); //default 접근 제어
        nested.innerInstanceMethod();
    }
}
