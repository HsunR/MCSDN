package com.blog.service;

import com.blog.entity.Article;
import com.blog.dto.ArticleRequest;

import java.util.List;

public interface ArticleService {
    List<Article> getAllArticles();
    Article getArticleById(Long id);
    Article createArticle(ArticleRequest request);
    Article updateArticle(Long id, ArticleRequest request);
    void deleteArticle(Long id);
}
