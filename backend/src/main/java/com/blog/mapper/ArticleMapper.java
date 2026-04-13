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
}
