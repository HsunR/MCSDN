package com.blog.service;

import com.blog.dto.CsdnArticleDto;

/**
 * Parses CSDN article HTML to extract structured data.
 * Uses Jsoup CSS selectors per D-01 and D-06.
 */
public interface CsdnArticleParser {
    /**
     * Parses raw CSDN article HTML into structured DTO.
     * @param html Raw HTML content from CSDN article page
     * @param url Original URL (for extracting articleId)
     * @return Parsed article DTO with title, content (Markdown), tags
     */
    CsdnArticleDto parseArticle(String html, String url);
}
