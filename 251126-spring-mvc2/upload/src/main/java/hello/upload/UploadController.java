package hello.upload;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Controller
public class UploadController {
    @Value("${file.dir}")
    private String fileDir;
    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }
    @PostMapping("/upload")
    public String saveFile(HttpServletRequest request) throws ServletException, IOException {
        String itemName = request.getParameter("itemName");
        System.out.println();
        System.out.println("itemName = " + itemName);
        Collection<Part> parts = request.getParts();
        for (Part part : parts) {
            Collection<String> headerNames = part.getHeaderNames();
            InputStream inputStream = part.getInputStream();
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            System.out.println();
            System.out.println("headerNames = " + headerNames);
            System.out.println("body = " + body);
            if (StringUtils.hasText(part.getSubmittedFileName())) {
                String filePath = fileDir + part.getSubmittedFileName();
                part.write(filePath);
            }
        }
        return "upload-form";
    }
}
