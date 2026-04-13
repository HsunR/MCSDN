package com.blog.dto;

import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PublicArticleResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private LocalDateTime createdAt;
    private CategoryDTO category;
    private List<TagDTO> tags;

    public PublicArticleResponse() {}

    public static PublicArticleResponse fromArticle(Article article, Category category, List<Tag> tags) {
        PublicArticleResponse response = new PublicArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setSlug(generateSlug(article.getTitle()));
        response.setExcerpt(generateExcerpt(article.getContent()));
        response.setContent(article.getContent());
        response.setCreatedAt(article.getCreatedAt());
        if (category != null) {
            response.setCategory(new CategoryDTO(category.getId(), category.getName(), generateSlug(category.getName())));
        }
        if (tags != null) {
            response.setTags(tags.stream()
                .map(t -> new TagDTO(t.getId(), t.getName(), generateSlug(t.getName())))
                .collect(Collectors.toList()));
        }
        return response;
    }

    public static String generateExcerpt(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        String stripped = markdown
            .replaceAll("#+\\s*", "")
            .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
            .replaceAll("\\*([^*]+)\\*", "$1")
            .replaceAll("__([^_]+)__", "$1")
            .replaceAll("_([^_]+)_", "$1")
            .replaceAll("```[\\s\\S]*?```", "")
            .replaceAll("`([^`]+)`", "$1")
            .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1")
            .replaceAll("!\\[([^\\]]*)\\]\\([^)]+\\)", "")
            .replaceAll("\\n+", " ")
            .replaceAll("\\s+", " ")
            .trim();
        if (stripped.length() <= 150) {
            return stripped;
        }
        return stripped.substring(0, 147) + "...";
    }

    public static String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        String slug = Pattern.compile("[^a-z0-9\\s-]", Pattern.CASE_INSENSITIVE)
            .matcher(title.toLowerCase().replaceAll("\\s+", "-"))
            .replaceAll("");
        return slug.replaceAll("-+", "-").replaceAll("^-|-$", "");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    public static class CategoryDTO {
        private Long id;
        private String name;
        private String slug;

        public CategoryDTO() {}

        public CategoryDTO(Long id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }
    }

    public static class TagDTO {
        private Long id;
        private String name;
        private String slug;

        public TagDTO() {}

        public TagDTO(Long id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }
    }
}
