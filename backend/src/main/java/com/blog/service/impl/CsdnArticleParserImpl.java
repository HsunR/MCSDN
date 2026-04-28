package com.blog.service.impl;

import com.blog.dto.CsdnArticleDto;
import com.blog.service.CsdnArticleParser;
import com.blog.service.HtmlToMarkdownConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CsdnArticleParserImpl implements CsdnArticleParser {

    private static final Logger log = LoggerFactory.getLogger(CsdnArticleParserImpl.class);

    // CSS selectors per D-02 (primary with fallbacks)
    // Updated for current CSDN HTML structure (2025)
    private static final String TITLE_SELECTOR = "h1.title-article";
    private static final String TITLE_FALLBACK = "h1";
    private static final String CONTENT_SELECTOR = "div#content_views, div#article_content, div.article_content";
    private static final String CONTENT_FALLBACK = "div.htmledit_views";
    // Tag selectors - used in parseArticle method
    private static final String TAG_SELECTOR = "a.tag-link-new, a.tag";
    private static final String PUBLISHED_DATE_SELECTOR = "span.time";
    private static final DateTimeFormatter CSDN_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern CSDN_DATE_TEXT_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2})");

    // Pre-compiled patterns for article ID extraction (performance optimization)
    private static final Pattern ARTICLE_ID_PATTERN_DETAILS = Pattern.compile("/article/details/(\\d+)");
    private static final Pattern ARTICLE_ID_PATTERN_HTML = Pattern.compile("/article/(\\d{8,})\\.html$");
    private static final Pattern ARTICLE_ID_PATTERN_SHORT = Pattern.compile("/p/(\\d+)");

    @Autowired
    private HtmlToMarkdownConverter htmlToMarkdownConverter;

    @Override
    public CsdnArticleDto parseArticle(String html, String url) {
        // Parse HTML directly without sanitization (CSDN is trusted source)
        Document doc = Jsoup.parse(html);

        CsdnArticleDto dto = new CsdnArticleDto();
        dto.setUrl(url);
        dto.setArticleId(extractArticleId(url));

        // Extract title
        Element titleEl = doc.selectFirst(TITLE_SELECTOR);
        if (titleEl == null) {
            titleEl = doc.selectFirst(TITLE_FALLBACK);
        }
        dto.setTitle(titleEl != null ? titleEl.text().trim() : "Untitled");

        // Extract content and convert to Markdown
        Element contentEl = doc.selectFirst(CONTENT_SELECTOR);
        if (contentEl == null) {
            contentEl = doc.selectFirst(CONTENT_FALLBACK);
        }
        if (contentEl != null) {
            String contentHtml = contentEl.html();
            log.debug("Content HTML preview (first 300 chars): {}", contentHtml.substring(0, Math.min(300, contentHtml.length())).replace("\n", " "));
            String markdown = htmlToMarkdownConverter.convert(contentHtml);
            dto.setContent(markdown);
            log.debug("Converted markdown preview (first 200 chars): {}", markdown.substring(0, Math.min(200, markdown.length())));
        } else {
            dto.setContent("");
            log.warn("Could not find content element for article: {}", url);
        }

        // Extract tags using the defined selector constant
        List<String> tags = new ArrayList<>();
        for (Element tag : doc.select(TAG_SELECTOR)) {
            String tagText = tag.text().trim();
            // Remove leading # if present and add non-empty tags
            if (!tagText.isEmpty()) {
                tags.add(tagText.startsWith("#") ? tagText.substring(1) : tagText);
            }
        }
        dto.setTags(tags);

        // Extract published date from span.time (handles multiple CSDN formats)
        Element timeEl = doc.selectFirst(PUBLISHED_DATE_SELECTOR);
        if (timeEl != null) {
            // Try data-time attribute first (newer CSDN format: <span class="time blog-postTime" data-time="2025-10-20 13:08:33">)
            String dateStr = timeEl.attr("data-time");
            if (dateStr == null || dateStr.isEmpty()) {
                // Fall back to text content (older format: "于 2025-10-20 13:08:33 发布" or "已于 2024-01-08 15:50:24 修改")
                String timeText = timeEl.text();
                dateStr = extractDateFromTimeText(timeText);
            }
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    dto.setPublishedAt(LocalDateTime.parse(dateStr, CSDN_DATE_FORMATTER));
                } catch (DateTimeParseException e) {
                    log.warn("Failed to parse date '{}' from article: {}", dateStr, url);
                }
            }
        }

        log.debug("Parsed article: title={}, tags={}, articleId={}, publishedAt={}", dto.getTitle(), dto.getTags(), dto.getArticleId(), dto.getPublishedAt());
        return dto;
    }

    private String extractDateFromTimeText(String timeText) {
        // Matches both "于 2025-10-20 13:08:33 发布" and "已于 2024-01-08 15:50:24 修改"
        Matcher m = CSDN_DATE_TEXT_PATTERN.matcher(timeText);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    private String extractArticleId(String url) {
        // CSDN URL formats:
        // 1. https://blog.csdn.net/user/article/details/12345678  (current)
        // 2. https://blog.csdn.net/user/article/12345678.html     (legacy)
        // 3. https://blog.csdn.net/user/p/12345678                (older format)

        // Pattern 1: /article/details/{number} - most common current format
        Matcher m1 = ARTICLE_ID_PATTERN_DETAILS.matcher(url);
        if (m1.find()) {
            return m1.group(1);
        }

        // Pattern 2: /article/{number}.html - legacy format
        Matcher m2 = ARTICLE_ID_PATTERN_HTML.matcher(url);
        if (m2.find()) {
            return m2.group(1);
        }

        // Pattern 3: /p/{number} - older short URL format
        Matcher m3 = ARTICLE_ID_PATTERN_SHORT.matcher(url);
        if (m3.find()) {
            return m3.group(1);
        }

        log.warn("Could not extract article ID from URL: {}, using full URL", url);
        return url;
    }
}
