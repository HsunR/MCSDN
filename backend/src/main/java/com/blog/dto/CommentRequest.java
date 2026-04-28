package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    @NotBlank(message = "Author name is required")
    @Size(min = 1, max = 50, message = "Author name must be between 1 and 50 characters")
    private String authorName;

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String content;

    private String honeypot;

    @Size(max = 200, message = "Website URL is too long")
    private String website;

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getHoneypot() { return honeypot; }
    public void setHoneypot(String honeypot) { this.honeypot = honeypot; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
}
