package com.blog.service.impl;

import com.blog.entity.Category;
import com.blog.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import com.blog.util.SlugUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getAll() {
        return categoryMapper.findAll();
    }

    @Override
    public Category getById(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new RuntimeException("Category not found");
        }
        return category;
    }

    @Override
    public Category create(String name) {
        if (categoryMapper.countByName(name) > 0) {
            throw new RuntimeException("Category already exists");
        }
        Category category = new Category();
        category.setName(name);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Long id, String name) {
        Category existing = categoryMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("Category not found");
        }
        if (categoryMapper.countByName(name) > 0) {
            Category byName = categoryMapper.findByName(name);
            if (byName != null && !byName.getId().equals(id)) {
                throw new RuntimeException("Category already exists");
            }
        }
        existing.setName(name);
        existing.setSlug(SlugUtils.slugify(name));
        categoryMapper.update(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        categoryMapper.delete(id);
    }

    @Override
    public Category getBySlug(String slug) {
        return categoryMapper.findByName(slug);
    }
}
