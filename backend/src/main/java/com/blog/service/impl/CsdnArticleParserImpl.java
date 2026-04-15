package com.blog.service.impl;

import com.blog.dto.CsdnArticleDto;
import com.blog.service.CsdnArticleParser;
import com.blog.service.HtmlToMarkdownConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
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
    private static final String TITLE_SELECTOR = "h1.title-article";
    private static final String TITLE_FALLBACK = "h1";
    private static final String CONTENT_SELECTOR = "div.article_content";
    private static final String CONTENT_FALLBACK = "div.article-content";
    private static final String TAG_SELECTOR = "div.tag-list-box a.tag";
    private static final String TAG_FALLBACK = "div.tags a";

    @Autowired
    private HtmlToMarkdownConverter htmlToMarkdownConverter;

    @Override
    public CsdnArticleDto parseArticle(String html, String url) {
        // Sanitize HTML first to prevent XSS per security controls
        String safeHtml = Jsoup.clean(html, Safelist.none());
        Document doc = Jsoup.parse(safeHtml);

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
        // Extract article ID from CSDN URL pattern: https://blog.csdn.net/user/article/12345678
        Pattern pattern = Pattern.compile("/(\\d{8,})\\.html$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Fallback: try to extract from anywhere in URL
        Pattern altPattern = Pattern.compile("article[/_](\\d+)");
        Matcher altMatcher = altPattern.matcher(url);
        if (altMatcher.find()) {
            return altMatcher.group(1);
        }
        return url;
    }
}
