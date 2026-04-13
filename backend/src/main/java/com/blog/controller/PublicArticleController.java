package com.blog.controller;

import com.blog.dto.ArticleListRequest;
import com.blog.dto.ArticleListResponse;
import com.blog.dto.PublicArticleResponse;
import com.blog.service.PublicArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class PublicArticleController {

    private final PublicArticleService publicArticleService;

    public PublicArticleController(PublicArticleService publicArticleService) {
        this.publicArticleService = publicArticleService;
    }

    @GetMapping
    public ResponseEntity<ArticleListResponse> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (page < 1) page = 1;
        if (pageSize > 100) pageSize = 100;
        if (pageSize < 1) pageSize = 10;
        ArticleListRequest request = new ArticleListRequest(page, pageSize);
        return ResponseEntity.ok(publicArticleService.getArticles(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicArticleResponse> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(publicArticleService.getArticleById(id));
    }

    @GetMapping("/category/{categorySlug}")
    public ResponseEntity<ArticleListResponse> getByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(publicArticleService.getArticlesByCategory(categorySlug, page, pageSize));
    }

    @GetMapping("/tag/{tagSlug}")
    public ResponseEntity<ArticleListResponse> getByTag(
            @PathVariable String tagSlug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(publicArticleService.getArticlesByTag(tagSlug, page, pageSize));
    }

    @GetMapping("/search")
    public ResponseEntity<ArticleListResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(publicArticleService.searchArticles(q, page, pageSize));
    }
}
