package com.blog.service;

import com.blog.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAll();
    Category getById(Long id);
    Category create(String name);
    Category update(Long id, String name);
    void delete(Long id);
    Category getBySlug(String slug);
}
