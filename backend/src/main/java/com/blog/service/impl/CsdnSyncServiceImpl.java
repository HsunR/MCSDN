package com.blog.service.impl;

import com.blog.dto.CsdnArticleDto;
import com.blog.dto.SyncResultResponse;
import com.blog.entity.Article;
import com.blog.entity.CsdnSyncConfig;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.CsdnArticleFetcher;
import com.blog.service.CsdnArticleParser;
import com.blog.service.CsdnSyncConfigService;
import com.blog.service.CsdnSyncService;
import com.blog.service.ImageDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class CsdnSyncServiceImpl implements CsdnSyncService {

    private static final Logger log = LoggerFactory.getLogger(CsdnSyncServiceImpl.class);
    private static final String SOURCE_CSDN = "CSDN";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    @Autowired
    private CsdnSyncConfigService csdnSyncConfigService;

    @Autowired
    private CsdnArticleFetcher csdnArticleFetcher;

    @Autowired
    private CsdnArticleParser csdnArticleParser;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ImageDownloadService imageDownloadService;

    @Override
    @Transactional
    public SyncResultResponse syncArticles() {
        CsdnSyncConfig config = csdnSyncConfigService.getConfig();

        if (config == null || config.getCsdnUserId() == null || config.getCsdnUserId().isEmpty()) {
            log.warn("CSDN sync config not set up");
            return new SyncResultResponse(0, 0, 0, List.of("CSDN sync not configured"));
        }

        if (config.getCategoryId() == null) {
            log.warn("CSDN sync category not set");
            return new SyncResultResponse(0, 0, 0, List.of("Target category not configured"));
        }

        log.info("Starting CSDN sync for userId: {}", config.getCsdnUserId());

        // Fetch all article URLs from CSDN
        List<String> articleUrls = csdnArticleFetcher.fetchArticleList(config.getCsdnUserId());
        log.info("Found {} articles to sync", articleUrls.size());

        int created = 0, updated = 0, skipped = 0;
        List<String> errors = new ArrayList<>();

        for (String articleUrl : articleUrls) {
            try {
                // Fetch and parse article
                String html = csdnArticleFetcher.fetchArticleHtml(articleUrl);
                CsdnArticleDto articleDto = csdnArticleParser.parseArticle(html, articleUrl);

                // Replace CSDN image URLs with local paths (SYNC-07, SYNC-09)
                try {
                    String contentWithLocalImages = imageDownloadService.downloadAndReplaceImages(articleDto.getContent());
                    articleDto.setContent(contentWithLocalImages);
                } catch (Exception e) {
                    // Image download failed — continue with original content per continue-on-error pattern
                    log.warn("Image download failed for article {}: {}", articleUrl, e.getMessage());
                }

                // Compute content hash for update detection per D-03
                String contentHash = computeMd5Hash(articleDto.getContent());

                // Check if article already exists by csdn_article_id
                Article existing = articleMapper.findByCsdnArticleId(articleDto.getArticleId());

                if (existing == null) {
                    // New article - create it
                    Article article = new Article();
                    article.setTitle(articleDto.getTitle());
                    article.setContent(articleDto.getContent());
                    article.setStatus(STATUS_PUBLISHED);
                    article.setCategoryId(config.getCategoryId());
                    article.setSource(SOURCE_CSDN);
                    article.setCsdnArticleId(articleDto.getArticleId());
                    article.setContentHash(contentHash);
                    // Set timestamps from CSDN published date, fallback to now
                    LocalDateTime publishedAt = articleDto.getPublishedAt();
                    LocalDateTime now = LocalDateTime.now();
                    article.setCreatedAt(publishedAt != null ? publishedAt : now);
                    article.setUpdatedAt(publishedAt != null ? publishedAt : now);
                    articleMapper.insert(article);
                    syncArticleTags(article.getId(), articleDto.getTags());
                    created++;
                    log.debug("Created article: {} ({})", articleDto.getTitle(), articleDto.getArticleId());
                } else {
                    // Check if content changed using hash per D-03
                    if (existing.getContentHash() != null && !existing.getContentHash().equals(contentHash)) {
                        // Content changed - update
                        LocalDateTime updatedAt = articleDto.getPublishedAt() != null ? articleDto.getPublishedAt() : LocalDateTime.now();
                        articleMapper.updateContentAndHash(existing.getId(), articleDto.getContent(), contentHash, updatedAt);
                        syncArticleTags(existing.getId(), articleDto.getTags());
                        updated++;
                        log.debug("Updated article: {} ({})", articleDto.getTitle(), articleDto.getArticleId());
                    } else {
                        // No changes - skip
                        skipped++;
                        log.debug("Skipped unchanged article: {} ({})", articleDto.getTitle(), articleDto.getArticleId());
                    }
                }

            } catch (Exception e) {
                // Continue on error per D-04
                log.error("Failed to sync article: {}", articleUrl, e);
                errors.add(articleUrl + ": " + e.getMessage());
            }
        }

        // Update last sync timestamp
        csdnSyncConfigService.updateLastSyncAt(LocalDateTime.now());

        log.info("CSDN sync complete: created={}, updated={}, skipped={}, errors={}",
                 created, updated, skipped, errors.size());

        return new SyncResultResponse(created, updated, skipped, errors);
    }

    /**
     * Computes MD5 hash of content for update detection.
     */
    private String computeMd5Hash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute MD5 hash", e);
        }
    }

    /**
     * Syncs tags for an article: creates missing tags and updates article_tags relationships.
     */
    private void syncArticleTags(Long articleId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        // Delete existing tag associations
        articleMapper.deleteArticleTags(articleId);
        // Create or find tags and associate
        for (String tagName : tagNames) {
            Tag tag = tagMapper.findByName(tagName);
            if (tag == null) {
                tag = new Tag();
                tag.setName(tagName);
                tagMapper.insert(tag);
            }
            articleMapper.insertArticleTag(articleId, tag.getId());
        }
    }
}
