package http6;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MemberRepository {
    private static final String FILE_PATH = Member.FILE_PATH;
    public static void registerMember(Member member) throws IOException {
        List<Member> members = readAllMembers();
        members.add(member);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            for (Member m : members) {
                oos.writeObject(m);
            }
        }
        System.out.println("회원이 등록되었습니다.");
    }
    public static List<Member> readAllMembers() {
        List<Member> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                try {
                    list.add((Member) ois.readObject());
                } catch (EOFException e) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
