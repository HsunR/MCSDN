package com.blog.service;

import java.util.List;

/**
 * Fetches article list and article HTML from CSDN.
 * Uses HTML scraping (Jsoup) per D-01 and D-06.
 */
public interface CsdnArticleFetcher {
    /**
     * Fetches all article URLs from CSDN blog for given userId.
     * @param csdnUserId CSDN user ID (e.g., "2301_78723800")
     * @return List of article URLs
     */
    List<String> fetchArticleList(String csdnUserId);

    /**
     * Fetches raw HTML content for a single CSDN article.
     * @param articleUrl Full URL of the CSDN article
     * @return Raw HTML content
     */
    String fetchArticleHtml(String articleUrl);
}
