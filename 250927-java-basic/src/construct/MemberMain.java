package construct;

import java.lang.reflect.Member;

public class MemberMain {
    public static void main(String[] args) {
        MemberInit member1 = new MemberInit();
        member1.initMember("user1", 1, 2);
        System.out.println("member1.name = " + member1.name);
        System.out.println("member1.age = " + member1.age);
        MemberInit member2 = new MemberInit("user2", 2, 3);
        System.out.println("member2.name = " + member2.name);
        System.out.println("member2.age = " + member2.age);
        MemberConstruct member3 = new MemberConstruct("user3", 3);
    }
}
