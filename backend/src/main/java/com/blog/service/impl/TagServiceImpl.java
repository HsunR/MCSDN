package com.blog.service.impl;

import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public List<Tag> getAll() {
        return tagMapper.findAll();
    }

    @Override
    public Tag getById(Long id) {
        Tag tag = tagMapper.findById(id);
        if (tag == null) {
            throw new RuntimeException("Tag not found");
        }
        return tag;
    }

    @Override
    public Tag create(String name) {
        if (tagMapper.countByName(name) > 0) {
            throw new RuntimeException("Tag already exists");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public void delete(Long id) {
        if (articleMapper.countByTagId(id) > 0) {
            throw new RuntimeException("Cannot delete tag that is assigned to articles");
        }
        tagMapper.delete(id);
    }

    @Override
    public Tag getBySlug(String slug) {
        return tagMapper.findByName(slug);
    }

    @Override
    public List<Tag> getTagsByArticleId(Long articleId) {
        return articleMapper.findTagsByArticleId(articleId);
    }
}
