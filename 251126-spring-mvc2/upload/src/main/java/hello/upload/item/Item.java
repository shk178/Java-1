package hello.upload.item;

import lombok.Data;
import java.util.List;

/**
 * 저장소(메모리/DB)에 저장되는 도메인 모델.
 * 서버에 저장된 파일 정보를 FileMetadata로 보유한다.
 */
@Data
public class Item {

    private Long id;
    private String name;

    private FileMetadata attachmentMeta;          // 단일 파일 메타데이터
    private List<FileMetadata> imageMetaList;     // 여러 이미지의 메타데이터

}
