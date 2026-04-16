package com.blog.service;

public interface ImageDownloadService {
    /**
     * Downloads and replaces CSDN image URLs in Markdown content.
     * Uses MD5 hash of URL for deduplication.
     * Retries failed downloads 3 times with exponential backoff (1s, 2s, 4s).
     * @param content Markdown content with CSDN image URLs
     * @return Content with replaced local paths; original URLs kept on failure
     */
    String downloadAndReplaceImages(String content);
}
