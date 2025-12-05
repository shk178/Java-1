package hello.upload.item;

import lombok.Data;

/**
 * 서버에 저장된 파일의 메타데이터를 표현하는 VO.
 * 실제 파일이 아니라 "원래 이름"과 "저장된 이름"만 갖는다.
 */
@Data
public class FileMetadata {

    private final String originalFileName;  // 클라이언트가 업로드한 원래 파일명
    private final String storedFileName;    // 서버에 저장한 UUID 기반 파일명

}
