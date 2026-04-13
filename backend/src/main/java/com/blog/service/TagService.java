package com.blog.service;

import com.blog.entity.Tag;

import java.util.List;

public interface TagService {
    List<Tag> getAll();
    Tag getById(Long id);
    Tag create(String name);
    void delete(Long id);
    Tag getBySlug(String slug);
    List<Tag> getTagsByArticleId(Long articleId);
}
