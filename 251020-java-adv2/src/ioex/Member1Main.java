package ioex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ioex.Member1.DELIMITER;
import static ioex.Member1.FILE_PATH;

public class Member1Main {
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
                    new Member1(id, name, age);
                    System.out.println("회원이 등록되었습니다.");
                    break;
                case 2:
                    File file = new File(FILE_PATH);
                    if (!file.exists()) {
                        System.out.println("등록된 회원이 없습니다.");
                        break;
                    }
                    try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] arr = line.split(DELIMITER);
                            System.out.println("id=" + arr[0] + ", name=" + arr[1] + ", age=" + arr[2]);
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
