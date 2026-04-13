package com.blog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/upload")
public class UploadController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid file type. Allowed: jpg, png, gif, webp"));
        }

        LocalDate now = LocalDate.now();
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null ? getExtension(originalFilename) : "jpg";
        String filename = UUID.randomUUID() + "." + ext;

        Path yearMonthDir = Paths.get(uploadPath,
            String.valueOf(now.getYear()),
            String.format("%02d", now.getMonthValue()));
        Path filePath = yearMonthDir.resolve(filename);

        try {
            Files.createDirectories(yearMonthDir);
            file.transferTo(filePath);
        } catch (IOException e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to save file"));
        }

        String url = "/uploads/" + now.getYear() + "/" +
            String.format("%02d", now.getMonthValue()) + "/" + filename;

        return ResponseEntity.ok(Map.of("url", url));
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "jpg";
    }
}
