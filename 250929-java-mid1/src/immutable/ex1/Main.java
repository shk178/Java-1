package immutable.ex1;

import immutable.ImmutableAddress;

public class Main {
    public static void main(String[] args) {
        ImmutableAddress address = new ImmutableAddress("서울");
        Member member1 = new Member("이름1", address);
        Member member2 = new Member("이름2", address);
        //System.out.println(member2.getAddress().address);
        System.out.println(member2.getAddress().getValue());
        //member2.getAddress().setValue("부산");
        member2.setAddress(new ImmutableAddress("부산"));
        System.out.println("member1 = " + member1);
        System.out.println("member2 = " + member2);
    }
}
