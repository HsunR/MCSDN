package com.blog.dto;

import java.util.List;

public class ArticleListResponse {
    private List<PublicArticleResponse> articles;
    private int page;
    private int pageSize;
    private long total;
    private int totalPages;

    public ArticleListResponse() {}

    public ArticleListResponse(List<PublicArticleResponse> articles, int page, int pageSize, long total) {
        this.articles = articles;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    public List<PublicArticleResponse> getArticles() {
        return articles;
    }

    public void setArticles(List<PublicArticleResponse> articles) {
        this.articles = articles;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
