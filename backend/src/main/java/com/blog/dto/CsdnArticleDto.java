package com.blog.dto;

import java.util.List;

public class CsdnArticleDto {
    private String articleId;
    private String title;
    private String content;
    private List<String> tags;
    private String url;

    public String getArticleId() { return articleId; }
    public void setArticleId(String articleId) { this.articleId = articleId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
