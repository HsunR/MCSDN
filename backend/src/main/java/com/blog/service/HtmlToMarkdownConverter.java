package com.blog.service;

/**
 * Converts HTML content to Markdown format.
 * Handles CSDN image URL replacement per SYNC-07.
 */
public interface HtmlToMarkdownConverter {
    /**
     * Converts HTML string to Markdown.
     * CSDN image URLs are replaced with local storage placeholders.
     * @param html Raw HTML content
     * @return Markdown formatted string
     */
    String convert(String html);
}
