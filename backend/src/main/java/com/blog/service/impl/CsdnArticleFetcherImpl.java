package com.blog.service.impl;

import com.blog.service.CsdnArticleFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CsdnArticleFetcherImpl implements CsdnArticleFetcher {

    private static final Logger log = LoggerFactory.getLogger(CsdnArticleFetcherImpl.class);

    private static final RestClient restClient = RestClient.create();

    @Override
    public List<String> fetchArticleList(String csdnUserId) {
        log.info("Fetching CSDN article list via JSON API for user: {}", csdnUserId);

        List<String> articleUrls = new ArrayList<>();
        int page = 1;
        int size = 100;
        int maxPages = 5; // Safety limit: 5 * 100 = 500 articles max

        while (page <= maxPages) {
            String apiUrl = "https://blog.csdn.net/community/home-api/v1/get-business-list"
                + "?username=" + csdnUserId
                + "&page=" + page
                + "&size=" + size
                + "&businessType=blog"
                + "&orderby=createtime"
                + "&noHtml=1";

            String jsonResponse = restClient
                .get()
                .uri(apiUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Referer", "https://blog.csdn.net/" + csdnUserId)
                .retrieve()
                .body(String.class);

            List<String> urls = parseArticleUrlsFromJson(jsonResponse);
            if (urls.isEmpty()) {
                log.warn("No article URLs found on page {}, stopping pagination", page);
                break;
            }
            articleUrls.addAll(urls);
            log.info("Page {}: found {} article URLs", page, urls.size());

            // Check if there are more pages by checking if list size == size
            if (urls.size() < size) {
                break; // Last page
            }
            page++;
        }

        log.info("Total article URLs fetched from CSDN: {}", articleUrls.size());
        return articleUrls;
    }

    @Override
    public String fetchArticleHtml(String articleUrl) {
        log.debug("Fetching article HTML from: {}", articleUrl);
        return restClient
            .get()
            .uri(articleUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Referer", "https://blog.csdn.net/")
            .retrieve()
            .body(String.class);
    }

    private List<String> parseArticleUrlsFromJson(String json) {
        List<String> urls = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return urls;
        }
        // Extract "url":"https://blog.csdn.net/..." patterns from JSON
        Pattern pattern = Pattern.compile("\"url\"\\s*:\\s*\"(https?://[^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (url != null && url.contains("blog.csdn.net")) {
                urls.add(url);
            }
        }
        return urls;
    }
}
