package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    @NotBlank
    @Size(max = 100)
    private String authorName;

    @NotBlank
    @Size(max = 5000)
    private String content;

    private String honeypot;

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getHoneypot() { return honeypot; }
    public void setHoneypot(String honeypot) { this.honeypot = honeypot; }
}
