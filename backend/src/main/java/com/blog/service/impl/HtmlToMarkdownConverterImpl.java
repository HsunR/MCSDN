package com.blog.service.impl;

import com.blog.service.HtmlToMarkdownConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class HtmlToMarkdownConverterImpl implements HtmlToMarkdownConverter {

    private static final Logger log = LoggerFactory.getLogger(HtmlToMarkdownConverterImpl.class);

    // CSDN image URL patterns to replace per SYNC-07
    private static final Pattern CSDN_IMAGE_PATTERN = Pattern.compile(
        "https?://img-blog\\.csdn\\.net/[^\\s\"']+\\.(jpg|jpeg|png|gif|webp)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CSDN_UPLOAD_PATTERN = Pattern.compile(
        "https?://upload\\.csdn\\.net/[^\\s\"']+\\.(jpg|jpeg|png|gif|webp)",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        Document doc = Jsoup.parseBodyFragment(html);
        log.debug("convert() parsing HTML fragment, body has {} children", doc.body().childNodes().size());
        StringBuilder markdown = new StringBuilder();

        for (Node node : doc.body().childNodes()) {
            if (node instanceof Element el) {
                log.debug("  child element: tag={}, class={}", el.tagName(), el.className());
            }
            processNode(node, markdown, 0);
        }

        String result = markdown.toString();
        // Replace CSDN image URLs with local placeholders per SYNC-07
        result = replaceCsdnImageUrls(result);
        return result.trim();
    }

    private void processNode(Node node, StringBuilder out, int indent) {
        if (node instanceof TextNode textNode) {
            String text = textNode.getWholeText().trim();
            if (!text.isEmpty()) {
                out.append(text);
            }
            return;
        }

        if (!(node instanceof Element el)) {
            return;
        }

        String tagName = el.tagName().toLowerCase();

        switch (tagName) {
            case "h1" -> out.append("# ").append(el.text()).append("\n\n");
            case "h2" -> out.append("## ").append(el.text()).append("\n\n");
            case "h3" -> out.append("### ").append(el.text()).append("\n\n");
            case "h4" -> out.append("#### ").append(el.text()).append("\n\n");
            case "h5" -> out.append("##### ").append(el.text()).append("\n\n");
            case "h6" -> out.append("###### ").append(el.text()).append("\n\n");
            case "p" -> {
                for (Node child : el.childNodes()) {
                    processNode(child, out, 0);
                }
                out.append("\n\n");
            }
            case "br" -> out.append("\n");
            case "strong", "b" -> out.append("**").append(el.text()).append("**");
            case "em", "i" -> out.append("*").append(el.text()).append("*");
            case "code" -> {
                if (el.parent() != null && "pre".equals(el.parent().tagName().toLowerCase())) {
                    out.append(el.text());
                } else {
                    out.append("`").append(el.text()).append("`");
                }
            }
            case "pre" -> {
                // Try to extract language from class (e.g., "language-python hljs")
                String lang = extractLanguage(el);
                // Try to extract code content handling line numbers
                String code = extractPreCode(el);
                out.append("```").append(lang).append("\n").append(code).append("\n```\n\n");
            }
            case "blockquote" -> {
                out.append("> ").append(el.text().replace("\n", "\n> ")).append("\n\n");
            }
            case "a" -> {
                String href = el.attr("href");
                String text = el.text();
                out.append("[").append(text).append("](").append(href).append(")");
            }
            case "img" -> {
                // Handle lazy-loaded images: CSDN uses data-src for lazy loading
                String src = el.attr("src");
                String dataSrc = el.attr("data-src");
                // Use data-src if src is empty, placeholder, or a tracking pixel
                if ((src == null || src.isEmpty() || isPlaceholderSrc(src)) && dataSrc != null && !dataSrc.isEmpty()) {
                    src = dataSrc;
                }
                String alt = el.attr("alt");
                if (alt == null || alt.isEmpty()) {
                    alt = el.attr("title");
                }
                if (src != null && !src.isEmpty() && !isPlaceholderSrc(src)) {
                    out.append("![").append(alt != null ? alt : "").append("](").append(src).append(")");
                }
            }
            case "ul" -> {
                for (Element li : el.select("> li")) {
                    out.append(" ".repeat(indent)).append("- ").append(li.text()).append("\n");
                }
                out.append("\n");
            }
            case "ol" -> {
                int i = 1;
                for (Element li : el.select("> li")) {
                    out.append(" ".repeat(indent)).append(i++).append(". ").append(li.text()).append("\n");
                }
                out.append("\n");
            }
            case "div", "span", "section", "article" -> {
                for (Node child : el.childNodes()) {
                    processNode(child, out, indent);
                }
            }
            default -> {
                for (Node child : el.childNodes()) {
                    processNode(child, out, indent);
                }
            }
        }
    }

    private String processInline(String text) {
        return text;
    }

    /**
     * Extract language from pre/code element class attributes.
     * CSDN uses: class="language-python hljs" or class="set-code-height hljs"
     */
    private String extractLanguage(Element pre) {
        // Check pre element first
        String cls = pre.className();
        String lang = extractLangFromClass(cls);
        if (!lang.isEmpty()) return lang;

        // Check code child element
        Element codeEl = pre.selectFirst("code");
        if (codeEl != null) {
            cls = codeEl.className();
            lang = extractLangFromClass(cls);
        }
        return lang;
    }

    private String extractLangFromClass(String className) {
        if (className == null) return "";
        // Match "language-xxx" pattern
        for (String part : className.split("\\s+")) {
            if (part.startsWith("language-")) {
                return part.substring("language-".length());
            }
        }
        return "";
    }

    /**
     * Extract code content from pre element, handling CSDN's complex nested structure.
     * CSDN wraps code in: pre > code > ol.hljs-ln > li.hljs-ln-line > div.hljs-ln-code > div.hljs-ln-line
     * Also handles: pre > code directly with text()
     */
    private String extractPreCode(Element pre) {
        StringBuilder code = new StringBuilder();
        Element codeEl = pre.selectFirst("code");
        if (codeEl == null) {
            return pre.text();
        }

        // Check for CSDN line number structure: div.hljs-ln-numbers > div.hljs-ln-line
        // Code content lives in div.hljs-ln-code > div.hljs-ln-line
        List<Element> lines = codeEl.select("div.hljs-ln-code > div.hljs-ln-line");
        if (!lines.isEmpty()) {
            for (Element line : lines) {
                code.append(line.text()).append("\n");
            }
            return code.toString().trim();
        }

        // Fallback: try direct li selection for other line-numbered formats
        lines = codeEl.select("li.hljs-ln-line");
        if (!lines.isEmpty()) {
            for (Element line : lines) {
                // Get direct text content, skipping line number divs
                String text = line.selectFirst("div.hljs-ln-code") != null
                    ? line.selectFirst("div.hljs-ln-code").text()
                    : line.text();
                code.append(text).append("\n");
            }
            return code.toString().trim();
        }

        // Simple case: just code element text
        return codeEl.text().trim();
    }

    private String replaceCsdnImageUrls(String markdown) {
        String result = markdown;
        java.util.regex.Matcher m = CSDN_IMAGE_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        int replaced = 0;
        while (m.find()) {
            String url = m.group();
            String localPath = urlToLocalPath(url);
            m.appendReplacement(sb, localPath);
            replaced++;
        }
        m.appendTail(sb);
        result = sb.toString();

        m = CSDN_UPLOAD_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (m.find()) {
            String url = m.group();
            String localPath = urlToLocalPath(url);
            m.appendReplacement(sb, localPath);
            replaced++;
        }
        m.appendTail(sb);
        if (replaced > 0) {
            log.info("Replaced {} CSDN image URLs with local paths", replaced);
        }
        return sb.toString();
    }

    private String urlToLocalPath(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(url.getBytes(StandardCharsets.UTF_8));
            String hash = HexFormat.of().formatHex(digest);
            String ext = "jpg";
            int dotIdx = url.lastIndexOf('.');
            if (dotIdx > 0) {
                ext = url.substring(dotIdx + 1);
                if (ext.length() > 4) ext = "jpg";
            }
            return "/uploads/csdn/" + hash.substring(0, 16) + "." + ext;
        } catch (NoSuchAlgorithmException e) {
            return "/uploads/csdn/" + url.hashCode() + ".jpg";
        }
    }

    /**
     * Check if src is a placeholder/tracking pixel that should be ignored.
     */
    private boolean isPlaceholderSrc(String src) {
        if (src == null || src.isEmpty()) return true;
        String lower = src.toLowerCase();
        return lower.contains("loading") ||
               lower.contains("placeholder") ||
               lower.contains("blank.gif") ||
               lower.contains("data:image") ||
               lower.contains("1.png") ||   // kunyu.csdn.net ads
               lower.contains("avatar.csdnimg.cn/default");
    }
}
