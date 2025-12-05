package hello.upload.item;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Item 생성, 조회, 파일 다운로드 등을 처리하는 Controller.
 */
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStorage fileStorage;

    /**
     * 등록 폼
     */
    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemRequest form) {
        return "item-form";
    }

    /**
     * Item 저장
     */
    @PostMapping("/items/new")
    public String saveItem(
            @ModelAttribute ItemRequest form,
            RedirectAttributes redirectAttributes) throws IOException {

        // 1. 파일 저장
        FileMetadata attachment = fileStorage.storeFile(form.getUploadedAttachment());
        List<FileMetadata> images = fileStorage.storeFiles(form.getUploadedImages());

        // 2. 도메인 모델 생성
        Item item = new Item();
        item.setName(form.getRequestItemName());
        item.setAttachmentMeta(attachment);
        item.setImageMetaList(images);

        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        return "redirect:/items/{itemId}";
    }

    /**
     * 상세 조회 화면
     */
    @GetMapping("/items/{itemId}")
    public String viewItem(@PathVariable Long itemId, Model model) {

        Item item = itemRepository.findById(itemId);
        ItemResponse response = toResponse(item);

        model.addAttribute("item", response);
        return "item-view";
    }

    /**
     * Item → ItemResponse 변환
     */
    private ItemResponse toResponse(Item item) {
        ItemResponse res = new ItemResponse();

        res.setResponseItemId(item.getId());
        res.setResponseItemName(item.getName());

        // 첨부파일 정보
        if (item.getAttachmentMeta() != null) {
            res.setAttachmentOriginalName(item.getAttachmentMeta().getOriginalFileName());
            res.setAttachmentDownloadUrl("/attach/" + item.getId());
        }

        // 이미지 정보
        if (item.getImageMetaList() != null) {
            List<String> originalNames = item.getImageMetaList().stream()
                    .map(FileMetadata::getOriginalFileName)
                    .toList();

            List<String> urls = item.getImageMetaList().stream()
                    .map(meta -> "/images/" + meta.getStoredFileName())
                    .toList();

            res.setImageOriginalNames(originalNames);
            res.setImageUrls(urls);
        }

        return res;
    }

    /**
     * 이미지 출력
     */
    @ResponseBody
    @GetMapping("/images/{fileName}")
    public Resource downloadImage(@PathVariable String fileName) throws MalformedURLException {
        return new UrlResource("file:" + fileStorage.fullPath(fileName));
    }

    /**
     * 첨부파일 다운로드
     */
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId)
            throws MalformedURLException {

        Item item = itemRepository.findById(itemId);
        FileMetadata meta = item.getAttachmentMeta();

        String storedName = meta.getStoredFileName();
        String originalName = meta.getOriginalFileName();

        UrlResource resource = new UrlResource("file:" + fileStorage.fullPath(storedName));

        // 파일명 UTF-8 인코딩
        String encodedName = UriUtils.encode(originalName, StandardCharsets.UTF_8);
        String disposition = "attachment; filename=\"" + encodedName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }

}
