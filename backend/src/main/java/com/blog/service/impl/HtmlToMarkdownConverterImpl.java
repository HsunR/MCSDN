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
        StringBuilder markdown = new StringBuilder();

        for (Node node : doc.body().childNodes()) {
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
                out.append(processInline(el.text()));
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
                out.append("```\n").append(el.text()).append("\n```\n\n");
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
                String src = el.attr("src");
                String alt = el.attr("alt");
                out.append("![").append(alt != null ? alt : "").append("](").append(src).append(")");
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
}
