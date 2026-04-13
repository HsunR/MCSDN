package com.blog.dto;

public class ArticleListRequest {
    private int page = 1;
    private int pageSize = 10;
    private String categorySlug;
    private String tagSlug;
    private String keyword;

    public ArticleListRequest() {}

    public ArticleListRequest(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page < 1 ? 1 : page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize > 100 ? 100 : (pageSize < 1 ? 10 : pageSize);
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getCategorySlug() {
        return categorySlug;
    }

    public void setCategorySlug(String categorySlug) {
        this.categorySlug = categorySlug;
    }

    public String getTagSlug() {
        return tagSlug;
    }

    public void setTagSlug(String tagSlug) {
        this.tagSlug = tagSlug;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getOffset() {
        return (getPage() - 1) * getPageSize();
    }

    public int getLimit() {
        return getPageSize();
    }
}
