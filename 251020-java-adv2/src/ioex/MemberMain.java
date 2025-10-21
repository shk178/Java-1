package ioex;

import java.io.*;
import java.util.Scanner;

public class MemberMain {
    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("1 회원등록 2 회원목록조회 3종료: ");
            int choice = input.nextInt();
            input.nextLine();
            switch (choice) {
                case 1:
                    System.out.print("id: ");
                    String id = input.nextLine();
                    System.out.print("name: ");
                    String name = input.nextLine();
                    System.out.print("age: ");
                    int age = input.nextInt();
                    input.nextLine();
                    new Member(id, name, age);
                    System.out.println("회원이 등록되었습니다.");
                    break;
                case 2:
                    File file = new File("temp/member.dat");
                    if (!file.exists()) {
                        System.out.println("등록된 회원이 없습니다.");
                        break;
                    }
                    try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
                        while (true) {
                            try {
                                String idRead = dis.readUTF();
                                String nameRead = dis.readUTF();
                                int ageRead = dis.readInt();
                                System.out.println("id=" + idRead + ", name=" + nameRead + ", age=" + ageRead);
                            } catch (EOFException e) {
                                break; // 파일 끝
                            }
                        }
                    }
                    break;
                case 3:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 시도하세요.");
            }
        }
    }
}
