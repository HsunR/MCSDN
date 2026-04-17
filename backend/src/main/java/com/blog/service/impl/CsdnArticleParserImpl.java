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
    private static final String CONTENT_FALLBACK = "div.article-content, div.htmledit_views";
    private static final String TAG_SELECTOR = "div.tags-box a.tag-link-new, div.tag-list-box a.tag";
    private static final String TAG_FALLBACK = "div.tags-box a, div.tags a";

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
            String markdown = htmlToMarkdownConverter.convert(contentEl.html());
            dto.setContent(markdown);
        } else {
            dto.setContent("");
            log.warn("Could not find content element for article: {}", url);
        }

        // Extract tags
        List<String> tags = new ArrayList<>();
        Element tagContainer = doc.selectFirst(TAG_SELECTOR);
        if (tagContainer == null) {
            tagContainer = doc.selectFirst(TAG_FALLBACK);
        }
        if (tagContainer != null) {
            // Select all tag links from within the container
            List<Element> tagElements = tagContainer.select("a.tag");
            if (tagElements.isEmpty()) {
                tagElements = tagContainer.select("a");
            }
            for (Element tag : tagElements) {
                String tagText = tag.text().trim();
                if (!tagText.isEmpty()) {
                    tags.add(tagText);
                }
            }
        }
        dto.setTags(tags);

        log.debug("Parsed article: title={}, tags={}, articleId={}", dto.getTitle(), dto.getTags(), dto.getArticleId());
        return dto;
    }

    private String extractArticleId(String url) {
        // CSDN URL formats:
        // 1. https://blog.csdn.net/user/article/details/12345678  (current)
        // 2. https://blog.csdn.net/user/article/12345678.html     (legacy)
        // 3. https://blog.csdn.net/user/p/12345678                (older format)

        // Pattern 1: /article/details/{number} - most common current format
        Pattern p1 = Pattern.compile("/article/details/(\\d+)");
        Matcher m1 = p1.matcher(url);
        if (m1.find()) {
            return m1.group(1);
        }

        // Pattern 2: /article/{number}.html - legacy format
        Pattern p2 = Pattern.compile("/article/(\\d{8,})\\.html$");
        Matcher m2 = p2.matcher(url);
        if (m2.find()) {
            return m2.group(1);
        }

        // Pattern 3: /p/{number} - older short URL format
        Pattern p3 = Pattern.compile("/p/(\\d+)");
        Matcher m3 = p3.matcher(url);
        if (m3.find()) {
            return m3.group(1);
        }

        log.warn("Could not extract article ID from URL: {}, using full URL", url);
        return url;
    }
}
