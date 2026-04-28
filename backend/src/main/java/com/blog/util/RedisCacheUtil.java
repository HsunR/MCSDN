package com.blog.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtil {

    private static final String ARTICLE_CACHE_PREFIX = "article::";
    private static final String ARTICLE_LIST_CACHE_PREFIX = "articleList::";
    private static final String CATEGORY_ARTICLES_PREFIX = "articlesByCategory::";
    private static final String TAG_ARTICLES_PREFIX = "articlesByTag::";
    private static final String SEARCH_PREFIX = "articleSearch::";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void deleteArticleCache(Long articleId) {
        String key = ARTICLE_CACHE_PREFIX + articleId;
        redisTemplate.delete(key);
    }

    public void deleteAllArticleListCaches() {
        Set<String> keys = redisTemplate.keys(ARTICLE_LIST_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void deleteCategoryArticleCaches(String categorySlug) {
        if (categorySlug != null) {
            String key = CATEGORY_ARTICLES_PREFIX + categorySlug;
            redisTemplate.delete(key);
        }
        Set<String> keys = redisTemplate.keys(CATEGORY_ARTICLES_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void deleteTagArticleCaches(String tagSlug) {
        if (tagSlug != null) {
            String key = TAG_ARTICLES_PREFIX + tagSlug;
            redisTemplate.delete(key);
        }
        Set<String> keys = redisTemplate.keys(TAG_ARTICLES_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void deleteSearchCaches() {
        Set<String> keys = redisTemplate.keys(SEARCH_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void clearAllArticleCaches() {
        deleteAllArticleListCaches();
        deleteSearchCaches();

        Set<String> articleKeys = redisTemplate.keys(ARTICLE_CACHE_PREFIX + "*");
        if (articleKeys != null && !articleKeys.isEmpty()) {
            redisTemplate.delete(articleKeys);
        }

        Set<String> categoryKeys = redisTemplate.keys(CATEGORY_ARTICLES_PREFIX + "*");
        if (categoryKeys != null && !categoryKeys.isEmpty()) {
            redisTemplate.delete(categoryKeys);
        }

        Set<String> tagKeys = redisTemplate.keys(TAG_ARTICLES_PREFIX + "*");
        if (tagKeys != null && !tagKeys.isEmpty()) {
            redisTemplate.delete(tagKeys);
        }
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
