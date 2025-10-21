package ioex;

import java.io.*;
import java.util.*;

public class Member2Main {
    private static final String FILE_PATH = Member2.FILE_PATH;
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("1 회원등록 2 회원목록조회 3 종료: ");
            int choice = input.nextInt();
            input.nextLine();
            switch (choice) {
                case 1 -> registerMember(input);
                case 2 -> showMembers();
                case 3 -> {
                    System.out.println("프로그램을 종료합니다.");
                    return;
                }
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }
    private static void registerMember(Scanner input) throws IOException {
        System.out.print("id: ");
        String id = input.nextLine();
        System.out.print("name: ");
        String name = input.nextLine();
        System.out.print("age: ");
        int age = input.nextInt();
        input.nextLine();
        Member2 member = new Member2(id, name, age);
        List<Member2> members = readAllMembers();
        members.add(member);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            for (Member2 m : members) {
                oos.writeObject(m);
            }
        }
        System.out.println("회원이 등록되었습니다.");
    }
    private static void showMembers() throws IOException, ClassNotFoundException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("등록된 회원이 없습니다.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                try {
                    Object o = ois.readObject();
                    System.out.println(o);
                } catch (EOFException e) {
                    break; // 파일 끝
                }
            }
        }
    }
    private static List<Member2> readAllMembers() {
        List<Member2> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                try {
                    list.add((Member2) ois.readObject());
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
