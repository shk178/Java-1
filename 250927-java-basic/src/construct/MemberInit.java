package construct;

public class MemberInit {
    String name;
    int age;
    int grade;
    void initMember(String a, int age, int c) {
        name = a;
        this.age = age;
        //age = age;
        this.grade = c;
    } //메서드
    MemberInit(){
        this.name = null;
        this.age = 0;
        this.grade = 0;
    }
    MemberInit(String name, int age, int grade) {
        this.name = name;
        this.age = age;
        this.grade = grade;
    }
}
