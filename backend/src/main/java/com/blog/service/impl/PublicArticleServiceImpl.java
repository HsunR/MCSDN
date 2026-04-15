package com.blog.service.impl;

import com.blog.dto.ArticleListRequest;
import com.blog.dto.ArticleListResponse;
import com.blog.dto.PublicArticleResponse;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.service.PublicArticleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicArticleServiceImpl implements PublicArticleService {

    private final ArticleMapper articleMapper;

    public PublicArticleServiceImpl(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    public ArticleListResponse getArticles(ArticleListRequest request) {
        List<Article> articles = articleMapper.findPublishedPaginated(
            request.getLimit(), request.getOffset());
        int total = articleMapper.countPublished();
        return buildResponse(articles, request.getPage(), request.getPageSize(), total);
    }

    @Override
    public PublicArticleResponse getArticleById(Long id) {
        Article article = articleMapper.findById(id);
        if (article == null || !"PUBLISHED".equals(article.getStatus())) {
            throw new ArticleNotFoundException("Article not found");
        }
        List<Tag> tags = articleMapper.findTagsByArticleId(id);
        return PublicArticleResponse.fromArticle(article, article.getCategory(), tags);
    }

    @Override
    public ArticleListResponse getArticlesByCategory(String categorySlug, int page, int pageSize) {
        ArticleListRequest request = new ArticleListRequest(page, pageSize);
        request.setCategorySlug(categorySlug);
        List<Article> articles = articleMapper.findByCategorySlugPaginated(
            categorySlug, request.getLimit(), request.getOffset());
        int total = articleMapper.countByCategorySlug(categorySlug);
        return buildResponse(articles, page, pageSize, total);
    }

    @Override
    public ArticleListResponse getArticlesByTag(String tagSlug, int page, int pageSize) {
        ArticleListRequest request = new ArticleListRequest(page, pageSize);
        request.setTagSlug(tagSlug);
        List<Article> articles = articleMapper.findByTagSlugPaginated(
            tagSlug, request.getLimit(), request.getOffset());
        int total = articleMapper.countByTagSlug(tagSlug);
        return buildResponse(articles, page, pageSize, total);
    }

    @Override
    public ArticleListResponse searchArticles(String keyword, int page, int pageSize) {
        ArticleListRequest request = new ArticleListRequest(page, pageSize);
        request.setKeyword(keyword);
        List<Article> articles = articleMapper.searchPublishedPaginated(
            keyword, request.getLimit(), request.getOffset());
        int total = articleMapper.countSearchPublished(keyword);
        return buildResponse(articles, page, pageSize, total);
    }

    private ArticleListResponse buildResponse(List<Article> articles, int page, int pageSize, int total) {
        List<PublicArticleResponse> responses = articles.stream()
            .map(article -> {
                List<Tag> tags = articleMapper.findTagsByArticleId(article.getId());
                return PublicArticleResponse.fromArticle(article, article.getCategory(), tags);
            })
            .toList();
        return new ArticleListResponse(responses, page, pageSize, total);
    }

    public static class ArticleNotFoundException extends RuntimeException {
        public ArticleNotFoundException(String message) {
            super(message);
        }
    }
}
