package com.blog.service.impl;

import com.blog.entity.DownloadedImage;
import com.blog.mapper.DownloadedImageMapper;
import com.blog.service.ImageDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageDownloadServiceImpl implements ImageDownloadService {

    private static final Logger log = LoggerFactory.getLogger(ImageDownloadServiceImpl.class);
    private static final int MAX_RETRIES = 3;

    // Matches Markdown images with http/https URLs ending in image extensions
    private static final Pattern IMAGE_PATTERN = Pattern.compile(
        "!\\[([^\\]]*)\\]\\((https?://[^)]+\\.(?:jpg|jpeg|png|gif|webp)(?:\\?[^)]*)?)\\)",
        Pattern.CASE_INSENSITIVE
    );

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    private static final RestClient restClient = RestClient.create();

    @Autowired
    private DownloadedImageMapper downloadedImageMapper;

    @Override
    public String downloadAndReplaceImages(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = IMAGE_PATTERN.matcher(content);

        while (matcher.find()) {
            String altText = matcher.group(1);
            String url = matcher.group(2);

            String localPath = processImage(url);
            if (localPath != null) {
                matcher.appendReplacement(result,
                    Matcher.quoteReplacement("![" + altText + "](" + localPath + ")"));
            } else {
                matcher.appendReplacement(result,
                    Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String processImage(String url) {
        // SSRF mitigation (T-05-01): only http and https schemes accepted
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            log.warn("Rejected non-http/https URL: {}", url);
            return null;
        }

        String urlHash = computeMd5(url);
        if (urlHash == null) {
            return null;
        }

        // MD5 dedup: check if already downloaded
        DownloadedImage existing = downloadedImageMapper.findByUrlHash(urlHash);
        if (existing != null) {
            log.debug("Cache hit for URL hash {}: {}", urlHash, existing.getLocalPath());
            return existing.getLocalPath();
        }

        // Download with exponential backoff retry
        byte[] imageBytes = null;
        String contentType = null;

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            if (attempt > 0) {
                try {
                    // Backoff: 1s, 2s, 4s before retries 1, 2, 3
                    Thread.sleep((long) Math.pow(2, attempt - 1) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Download retry interrupted for URL: {}", url);
                    return null;
                }
            }
            try {
                ResponseEntity<byte[]> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .toEntity(byte[].class);
                imageBytes = response.getBody();
                var ct = response.getHeaders().getContentType();
                contentType = ct != null ? ct.toString() : null;
                break;
            } catch (Exception e) {
                log.warn("Download attempt {}/{} failed for URL {}: {}", attempt + 1, MAX_RETRIES + 1, url, e.getMessage());
                if (attempt == MAX_RETRIES) {
                    log.error("All {} retries exhausted for URL: {}", MAX_RETRIES, url);
                    return null;
                }
            }
        }

        if (imageBytes == null) {
            return null;
        }

        // Infer extension from Content-Type header or URL (T-05-04)
        String ext = inferExtension(contentType, url);

        // Construct path: {uploadPath}/{year}/{month}/{urlHash}.{ext} (T-05-02)
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String filename = urlHash + "." + ext;

        Path dirPath = Paths.get(uploadPath, year, month);
        Path filePath = dirPath.resolve(filename);

        try {
            Files.createDirectories(dirPath);
            Files.write(filePath, imageBytes);
        } catch (IOException e) {
            log.error("Failed to write image to {}: {}", filePath, e.getMessage());
            return null;
        }

        String localPath = "/uploads/" + year + "/" + month + "/" + filename;

        // Record in downloaded_images table (T-05-03: parameterized query in mapper XML)
        DownloadedImage record = new DownloadedImage();
        record.setUrlHash(urlHash);
        record.setLocalPath(localPath);
        record.setOriginalUrl(url);
        downloadedImageMapper.insert(record);

        log.info("Downloaded image {} -> {}", url, localPath);
        return localPath;
    }

    private String computeMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not available", e);
            return null;
        }
    }

    private String inferExtension(String contentType, String url) {
        if (contentType != null) {
            String ct = contentType.toLowerCase();
            if (ct.contains("jpeg") || ct.contains("jpg")) return "jpg";
            if (ct.contains("png")) return "png";
            if (ct.contains("gif")) return "gif";
            if (ct.contains("webp")) return "webp";
        }
        // Fall back to URL extension
        String urlLower = url.toLowerCase();
        for (String ext : new String[]{"jpeg", "jpg", "png", "gif", "webp"}) {
            int idx = urlLower.indexOf("." + ext);
            if (idx >= 0) {
                return ext.equals("jpeg") ? "jpg" : ext;
            }
        }
        return "jpg";
    }
}
