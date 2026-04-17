package com.blog.service.impl;

import com.blog.service.CsdnArticleFetcher;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CsdnArticleFetcherImpl implements CsdnArticleFetcher {

    private static final Logger log = LoggerFactory.getLogger(CsdnArticleFetcherImpl.class);

    // Shared HTTP client with connection pooling
    private CloseableHttpClient httpClient;

    // Shared cookie store for session persistence (like Python requests.Session)
    private CookieStore cookieStore;

    // User agent string
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @PostConstruct
    public void init() {
        // Create a shared cookie store and HTTP client
        this.cookieStore = new BasicCookieStore();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(10);
        connManager.setDefaultMaxPerRoute(5);

        this.httpClient = HttpClients.custom()
            .setConnectionManager(connManager)
            .setUserAgent(USER_AGENT)
            .build();

        log.info("CsdnArticleFetcher initialized with HttpClient 5 and shared CookieStore");
    }

    @PreDestroy
    public void destroy() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            log.warn("Error closing HTTP client: {}", e.getMessage());
        }
    }

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

            String jsonResponse = httpGet(apiUrl, csdnUserId);
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                log.warn("Failed to fetch article list page {}", page);
                break;
            }

            // Check for Cloudflare challenge
            if (isCloudflareChallenge(jsonResponse)) {
                log.warn("Cloudflare challenge detected on article list page {}", page);
                break;
            }

            List<String> urls = parseArticleUrlsFromJson(jsonResponse);
            if (urls.isEmpty()) {
                log.warn("No article URLs found on page {}, stopping pagination", page);
                break;
            }
            articleUrls.addAll(urls);
            log.info("Page {}: found {} article URLs", page, urls.size());

            // Check if there are more pages
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

        // Retry up to 3 times with delay for Cloudflare WAF
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                String userId = extractUserId(articleUrl);
                String html = httpGet(articleUrl, userId);

                if (html == null || html.isEmpty()) {
                    log.warn("Attempt {}/{} failed for {}: received empty response", attempt, 3, articleUrl);
                    if (attempt < 3) {
                        Thread.sleep(2000L * attempt);
                    }
                    continue;
                }

                // Log first 200 chars for debugging
                if (html.length() > 200) {
                    log.debug("HTML preview for {}: {}", articleUrl, html.substring(0, Math.min(200, html.length())).replace("\n", " "));
                }

                // Check if we got a Cloudflare challenge page
                if (isCloudflareChallenge(html)) {
                    log.warn("Cloudflare challenge detected on attempt {}/{}, retrying...", attempt, 3);
                    if (attempt < 3) {
                        Thread.sleep(2000L * attempt); // Exponential backoff
                    }
                    continue;
                }

                return html;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Attempt {}/{} failed for {}: {}", attempt, 3, articleUrl, e.getMessage());
                if (attempt < 3) {
                    try {
                        Thread.sleep(2000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Failed to fetch article HTML after 3 attempts: {}", articleUrl);
        return null;
    }

    /**
     * Execute HTTP GET request with cookie persistence (like Python requests.Session).
     */
    private String httpGet(String url, String userId) {
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            request.setHeader("Referer", "https://blog.csdn.net/" + userId);

            CloseableHttpResponse response = httpClient.execute(request, context);
            try {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    log.warn("HTTP status {} for URL: {}", statusCode, url);
                    return null;
                }

                // Read response content using EntityUtils
                byte[] bodyBytes = EntityUtils.toByteArray(response.getEntity());
                return new String(bodyBytes, StandardCharsets.UTF_8);
            } finally {
                response.close();
            }
        } catch (IOException e) {
            log.error("IO error fetching {}: {}", url, e.getMessage());
        }
        return null;
    }

    /**
     * Check if response is a Cloudflare challenge page.
     */
    private boolean isCloudflareChallenge(String content) {
        if (content == null) return false;
        return content.contains("security verification") ||
               content.contains("Security Verification") ||
               content.contains("cloudflare") ||
               content.contains("请进行安全验证") ||
               content.contains("<title>404</title>") ||
               content.contains("\"pid\":\"404\"");
    }

    /**
     * Extract userId from CSDN article URL for Referer header.
     * URL format: https://blog.csdn.net/{userId}/article/details/{articleId}
     */
    private String extractUserId(String articleUrl) {
        int start = articleUrl.indexOf("blog.csdn.net/");
        if (start >= 0) {
            start += "blog.csdn.net/".length();
            int end = articleUrl.indexOf("/", start);
            if (end > start) {
                return articleUrl.substring(start, end);
            }
        }
        return "";
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