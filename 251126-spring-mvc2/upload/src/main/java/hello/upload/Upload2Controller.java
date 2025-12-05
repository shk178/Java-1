package hello.upload;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class Upload2Controller {
    @Value("${file.dir}")
    private String fileDir;
    @GetMapping("/upload2")
    public String newFile2() {
        return "upload-form";
    }
    @PostMapping("/upload2")
    public String saveFile2(
            @RequestParam String itemName,
            @RequestParam("file") MultipartFile multipartFile,
            HttpServletRequest request
    ) throws IOException {
        if(!multipartFile.isEmpty()) {
            String filePath = fileDir + multipartFile.getOriginalFilename();
            multipartFile.transferTo(new File(filePath));
        }
        return "upload-form";
    }
}
