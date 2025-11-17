package basic.lecture2;

public class Member {
    public Long id;
    public String name;
    public String grade;
    public Member(Long id, String name, String grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
    }
    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", grade='" + grade + '\'' +
                '}';
    }
}
