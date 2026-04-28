package com.blog.service;

import com.blog.entity.Portfolio;

import java.util.List;

public interface PortfolioService {
    List<Portfolio> getAll();
    Portfolio getById(Long id);
    Portfolio create(Portfolio portfolio);
    Portfolio update(Long id, Portfolio portfolio);
    void delete(Long id);
    List<Portfolio> getAllPublished();
}
