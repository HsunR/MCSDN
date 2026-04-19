package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.ArticleService;
import com.blog.dto.ArticleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<Article> getAllArticles() {
        List<Article> articles = articleMapper.findAll();
        for (Article article : articles) {
            article.setTags(articleMapper.findTagsByArticleId(article.getId()));
        }
        return articles;
    }

    @Override
    public Article getArticleById(Long id) {
        Article article = articleMapper.findById(id);
        if (article != null) {
            article.setTags(articleMapper.findTagsByArticleId(id));
        }
        return article;
    }

    @Override
    @Transactional
    public Article createArticle(ArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setStatus(request.getStatus());
        article.setCategoryId(request.getCategoryId());
        articleMapper.insert(article);

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tagName : request.getTags()) {
                Tag tag = tagMapper.findByName(tagName);
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(tagName);
                    tagMapper.insert(tag);
                }
                articleMapper.insertArticleTag(article.getId(), tag.getId());
            }
        }
        return getArticleById(article.getId());
    }

    @Override
    @Transactional
    public Article updateArticle(Long id, ArticleRequest request) {
        Article article = articleMapper.findById(id);
        if (article == null) {
            throw new RuntimeException("Article not found");
        }

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setStatus(request.getStatus());
        article.setCategoryId(request.getCategoryId());
        articleMapper.update(article);

        articleMapper.deleteArticleTags(id);
        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                Tag tag = tagMapper.findByName(tagName);
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(tagName);
                    tagMapper.insert(tag);
                }
                articleMapper.insertArticleTag(id, tag.getId());
            }
        }
        return getArticleById(id);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        articleMapper.deleteArticleTags(id);
        articleMapper.delete(id);
    }
}
