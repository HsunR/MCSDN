package com.blog.util;

import java.util.regex.Pattern;

public class XssFilter {

    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ON_EVENT_PATTERN = Pattern.compile("on\\w+\\s*=\\s*([\"'])[^\\1]*?\\1", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern IFRAME_PATTERN = Pattern.compile("<iframe[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern OBJECT_PATTERN = Pattern.compile("<object[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMBED_PATTERN = Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORM_PATTERN = Pattern.compile("<form[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ALLOWED_HTML_PATTERN = Pattern.compile("<(/?(b|i|u|em|strong|a|p|br|ul|ol|li|code|pre|blockquote|h[1-6]))[^>]*>", Pattern.CASE_INSENSITIVE);

    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String sanitized = input;

        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = IFRAME_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = OBJECT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = EMBED_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = FORM_PATTERN.matcher(sanitized).replaceAll("");

        sanitized = sanitized.replaceAll("<(?!(/?(b|i|u|em|strong|a|p|br|ul|ol|li|code|pre|blockquote|h[1-6]))\\s*[/]?)", "&lt;");
        sanitized = sanitized.replaceAll("(?<!</?(b|i|u|em|strong|a|p|br|ul|ol|li|code|pre|blockquote|h[1-6]))>", "&gt;");

        return sanitized.trim();
    }
}
