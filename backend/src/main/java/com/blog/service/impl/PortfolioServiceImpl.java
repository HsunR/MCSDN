package com.blog.service.impl;

import com.blog.entity.Portfolio;
import com.blog.mapper.PortfolioMapper;
import com.blog.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioMapper portfolioMapper;

    @Override
    public List<Portfolio> getAll() {
        return portfolioMapper.findAllOrderBySortOrder();
    }

    @Override
    public Portfolio getById(Long id) {
        Portfolio portfolio = portfolioMapper.findById(id);
        if (portfolio == null) {
            throw new RuntimeException("Portfolio not found");
        }
        return portfolio;
    }

    @Override
    public Portfolio create(Portfolio portfolio) {
        portfolioMapper.insert(portfolio);
        return portfolioMapper.findById(portfolio.getId());
    }

    @Override
    public Portfolio update(Long id, Portfolio portfolio) {
        Portfolio existing = portfolioMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("Portfolio not found");
        }
        existing.setTitle(portfolio.getTitle());
        existing.setCoverImage(portfolio.getCoverImage());
        existing.setArticleId(portfolio.getArticleId());
        existing.setSortOrder(portfolio.getSortOrder());
        portfolioMapper.update(existing);
        return portfolioMapper.findById(id);
    }

    @Override
    public void delete(Long id) {
        portfolioMapper.delete(id);
    }

    @Override
    public List<Portfolio> getAllPublished() {
        return portfolioMapper.findAllPublished();
    }
}
