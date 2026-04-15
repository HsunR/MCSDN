package com.blog.service.impl;

import com.blog.service.CsdnArticleFetcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class CsdnArticleFetcherImpl implements CsdnArticleFetcher {

    private static final Logger log = LoggerFactory.getLogger(CsdnArticleFetcherImpl.class);

    // Primary and fallback CSS selectors per D-02
    private static final String PRIMARY_SELECTOR = "div.article-list a.article-title";
    private static final String FALLBACK_SELECTOR = "div.post-list a.post-title";

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Override
    public List<String> fetchArticleList(String csdnUserId) {
        String url = "https://blog.csdn.net/" + csdnUserId + "/article/list/";
        log.info("Fetching CSDN article list from: {}", url);

        String html = fetchHtml(url);
        Document doc = Jsoup.parse(html);

        List<String> articleUrls = new ArrayList<>();

        // Try primary selector first
        List<Element> links = doc.select(PRIMARY_SELECTOR);
        if (links.isEmpty()) {
            log.warn("CSDN primary selector failed, trying fallback: {}", PRIMARY_SELECTOR);
            links = doc.select(FALLBACK_SELECTOR);
        }

        for (Element link : links) {
            String href = link.attr("href");
            if (href != null && !href.isEmpty()) {
                articleUrls.add(href);
            }
        }

        log.info("Found {} article URLs from CSDN", articleUrls.size());
        return articleUrls;
    }

    @Override
    public String fetchArticleHtml(String articleUrl) {
        log.debug("Fetching article HTML from: {}", articleUrl);
        return fetchHtml(articleUrl);
    }

    private String fetchHtml(String url) {
        return restClientBuilder.build()
            .get()
            .uri(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .retrieve()
            .body(String.class);
    }
}
