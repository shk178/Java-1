package files;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class FileMain {
    public static void main(String[] args) throws IOException {
        File file = new File("temp/example.txt");
        File dir = new File("temp/exampleDir");
        // exists(): 파일/디렉토리 존재 여부
        // new File()는 경로를 지정한 객체를 생성할 뿐, 실제 파일이 존재하지 않으면 exists()는 false를 반환
        System.out.println("file.exists() = " + file.exists());
        // createNewFile(): 새 파일 생성
        // example.txt 파일이 실제로 없었기 때문에 새로 생성되어 true를 반환
        System.out.println("file.createNewFile() = " + file.createNewFile());
        // mkdir(): 새 디렉토리 생성
        // exampleDir 디렉토리가 없었기 때문에 새로 생성되어 true를 반환
        System.out.println("dir.mkdir() = " + dir.mkdir());
        // delete(): 파일/디렉토리 삭제
        // 방금 만든 example.txt 파일을 삭제했기 때문에 true를 반환
        System.out.println("file.delete() = " + file.delete());
        // isFile(): 파일인지 확인
        // 파일을 삭제했기 때문에 false를 반환
        System.out.println("file.isFile() = " + file.isFile());
        // isDirectory(): 디렉토리인지 확인
        // exampleDir은 삭제되지 않았기 때문에 true를 반환
        System.out.println("dir.isDirectory() = " + dir.isDirectory());
        // getName(): 파일/디렉토리 이름 반환
        // File 객체는 경로만 기억하므로, 삭제되었더라도 이름은 example.txt 반환
        System.out.println("file.getName() = " + file.getName());
        // length(): 파일 크기를 바이트 단위로 반환
        // 삭제된 파일이므로 길이는 0
        System.out.println("file.length() = " + file.length());
        // renameTo(File dest): 파일 이름 변경 또는 이동
        // file은 이미 삭제된 상태이므로 renameTo()는 실패 - false를 반환
        File newFile = new File("temp/newExample.txt");
        System.out.println("file.renameTo(newFile) = " + file.renameTo(newFile));
        // lastModified(): 마지막으로 수정된 시간을 반환
        // newFile은 존재하지 않기 때문에 lastModified()는 0을 반환
        // Unix epoch 시간인 1970년 1월 1일로 표시
        System.out.println("new Date(newFile.lastModified()) = " + new Date(newFile.lastModified()));
    }
}
/*
file.exists() = false
file.createNewFile() = true
dir.mkdir() = true
file.delete() = true
file.isFile() = false
dir.isDirectory() = true
file.getName() = example.txt
file.length() = 0
file.renameTo(newFile) = false
new Date(newFile.lastModified()) = Thu Jan 01 09:00:00 KST 1970
 */