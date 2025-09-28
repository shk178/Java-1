package access.a;

public class AccessClass {
    public static void main(String[] args) {
        AccessClass3 accessClass3 = new AccessClass3();
        System.out.println(accessClass3.i + " " + accessClass3.j + " " + accessClass3.k); //4th
        AccessClass3.NestedClass0 nestedClass0 = new AccessClass3.NestedClass0();
        //AccessClass3.NestedClass1 nestedClass1 = new AccessClass3.NestedClass1();
        //AccessClass3.NestedClass2 nestedClass2 = new AccessClass3.NestedClass2();
    }
}

//public class AccessClass2 {}

class AccessClass3 {
    int i = 1, j = 1, k = 1;
    NestedClass1 nestedClass1_1 = new NestedClass1(); //2nd
    NestedClass2 nestedClass2_1 = new NestedClass2(); //3rd
    static class NestedClass0 {
        //i = 2;
        //NestedClass1 nestedClass1_2 = new NestedClass1();
        //NestedClass2 nestedClass2_3 = new NestedClass2();
        NestedClass0() {
            System.out.println("NestedClass0"); //5th
        }
    }
    class NestedClass1 {
        //j = 2;
        //NestedClass1 nestedClass1_3 = new NestedClass1();
        NestedClass2 nestedClass2_3 = new NestedClass2(); //1st
        NestedClass1() {
            System.out.println("NestedClass1");
        }
    }
    private class NestedClass2 {
        //k = 2;
        NestedClass2() {
            System.out.println("NestedClass2");
        }
    }
}
