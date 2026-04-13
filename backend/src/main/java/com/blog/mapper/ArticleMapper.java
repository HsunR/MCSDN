package com.blog.mapper;

import com.blog.entity.Article;
import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {
    List<Article> findAll();
    Article findById(@Param("id") Long id);
    int insert(Article article);
    int update(Article article);
    int delete(@Param("id") Long id);
    List<Tag> findTagsByArticleId(@Param("articleId") Long articleId);
    int insertArticleTag(@Param("articleId") Long articleId, @Param("tagId") Long tagId);
    int deleteArticleTags(@Param("articleId") Long articleId);
    List<Article> findPublishedPaginated(@Param("limit") int limit, @Param("offset") int offset);
    int countPublished();
    List<Article> findByCategorySlugPaginated(@Param("categorySlug") String slug, @Param("limit") int limit, @Param("offset") int offset);
    int countByCategorySlug(@Param("categorySlug") String slug);
    List<Article> findByTagSlugPaginated(@Param("tagSlug") String slug, @Param("limit") int limit, @Param("offset") int offset);
    int countByTagSlug(@Param("tagSlug") String slug);
    List<Article> searchPublishedPaginated(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);
    int countSearchPublished(@Param("keyword") String keyword);
}
