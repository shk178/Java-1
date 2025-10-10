package msq;

public class MemberRMain {
    public static void main(String[] args) {
        Member member1 = new Member("id1", "이름1");
        Member member2 = new Member("id2", "이름2");
        Member member3 = new Member("id3", "이름3");
        MemberRepo repo = new MemberRepo();
        repo.save(member1);
        repo.save(member2);
        repo.save(member3);
        System.out.println(repo.findById("id1"));
        System.out.println(repo.findByName("이름2"));
        repo.remove("id3");
        System.out.println(repo.findById("id3"));
    }
}
