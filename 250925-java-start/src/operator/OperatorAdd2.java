package operator;

public class OperatorAdd2 {
    public static void main(String[] args) {
        int a = 1;
        int b = 0;

        //전위 증감 연산자 사용 예
        b = ++a; //a를 먼저 증가시키고, 결과를 b에 대입
        System.out.println("a = " + a);
        System.out.println("b = " + b);

        //후위 증감 연산자 사용 예
        b = a++; //a를 b에 대입시키고, 다음에 a를 증가
        System.out.println("a = " + a);
        System.out.println("b = " + b);
    }
}
