package com.blog.controller;

import com.blog.entity.Portfolio;
import com.blog.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/portfolios")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<List<Portfolio>> getAll() {
        return ResponseEntity.ok(portfolioService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(portfolioService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Portfolio> create(@RequestBody Portfolio portfolio) {
        if (portfolio.getTitle() == null || portfolio.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (portfolio.getCoverImage() == null || portfolio.getCoverImage().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (portfolio.getArticleId() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (portfolio.getSortOrder() == null) {
            portfolio.setSortOrder(0);
        }
        try {
            Portfolio created = portfolioService.create(portfolio);
            return ResponseEntity.status(201).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Portfolio> update(@PathVariable Long id, @RequestBody Portfolio portfolio) {
        if (portfolio.getTitle() == null || portfolio.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (portfolio.getCoverImage() == null || portfolio.getCoverImage().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (portfolio.getArticleId() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(portfolioService.update(id, portfolio));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            portfolioService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<Portfolio>> getPublished() {
        return ResponseEntity.ok(portfolioService.getAllPublished());
    }
}
