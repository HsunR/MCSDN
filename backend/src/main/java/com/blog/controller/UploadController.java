package com.blog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
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

    private static final Map<String, byte[]> MAGIC_NUMBERS = Map.of(
        "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
        "png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
        "gif", new byte[]{0x47, 0x49, 0x46, 0x38},
        "webp", new byte[]{0x52, 0x49, 0x46, 0x46}
    );

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid file type. Allowed: jpg, png, gif, webp"));
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!validateMagicNumber(file, ext)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid file content. File does not match declared type."));
        }

        LocalDate now = LocalDate.now();
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

    private boolean validateMagicNumber(MultipartFile file, String ext) {
        try (InputStream is = file.getInputStream()) {
            byte[] magic = MAGIC_NUMBERS.get(ext.toLowerCase());
            if (magic == null) {
                return false;
            }
            byte[] fileHeader = is.readNBytes(magic.length);
            if (fileHeader.length != magic.length) {
                return false;
            }
            return Arrays.equals(fileHeader, magic);
        } catch (IOException e) {
            return false;
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "jpg";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "jpg";
    }
}
