package enumeration;

public class Main {
    public static void main(String[] args) {
        System.out.println("메인 시작");
        ClassGrade grade1 = ClassGrade.BASIC;
        System.out.println("BASIC");
        ClassGrade grade2 = ClassGrade.GOLD;
        System.out.println("GOLD");
        ClassGrade grade3 = ClassGrade.DIA;
        System.out.println("DIA");
        ClassGrade grade4 = ClassGrade.BASIC;
        ClassGrade grade5 = ClassGrade.GOLD;
        ClassGrade grade6 = ClassGrade.DIA;
        //메인 시작
        //1. 클래스 로딩 시작
        //→ BASIC 객체 생성
        //→ GOLD 객체 생성
        //→ DIA 객체 생성
        //5. 클래스 로딩 완료
        //BASIC
        //GOLD
        //DIA
        System.out.println(grade1 == grade2); //false
        System.out.println(grade1 == grade3); //false
        System.out.println(grade2 == grade3); //false
        System.out.println(grade1 == grade4); //true
        System.out.println(grade2 == grade5); //true
        System.out.println(grade3 == grade6); //true
    }
}
