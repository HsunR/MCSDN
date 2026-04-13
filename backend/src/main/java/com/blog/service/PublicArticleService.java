package com.blog.service;

import com.blog.dto.ArticleListRequest;
import com.blog.dto.ArticleListResponse;
import com.blog.dto.PublicArticleResponse;

public interface PublicArticleService {
    ArticleListResponse getArticles(ArticleListRequest request);
    PublicArticleResponse getArticleById(Long id);
    ArticleListResponse getArticlesByCategory(String categorySlug, int page, int pageSize);
    ArticleListResponse getArticlesByTag(String tagSlug, int page, int pageSize);
    ArticleListResponse searchArticles(String keyword, int page, int pageSize);
}
