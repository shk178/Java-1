package annotation;

public class Team {
    @NotEmpty(message = "팀 이름이 비어 있음")
    private String name;
    @Range(min = 1, max = 999, message = "회원 수는 1~999 사이")
    private int memberCount;
    public Team(String name, int memberCount) {
        this.name = name;
        this.memberCount = memberCount;
    }
    public String getName() {
        return name;
    }
    public int getMemberCount() {
        return memberCount;
    }
}
