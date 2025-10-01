package enumeration;

public class ClassGrade {
    static {
        System.out.println("1. 클래스 로딩 시작");
    }
    public static final ClassGrade BASIC = new ClassGrade("BASIC");
    public static final ClassGrade GOLD = new ClassGrade("GOLD");
    public static final ClassGrade DIA = new ClassGrade("DIA");
    static {
        System.out.println("5. 클래스 로딩 완료");
    }
    private String name;
    public ClassGrade(String name) {
        this.name = name;
        System.out.println("→ " + name + " 객체 생성");
    }
}