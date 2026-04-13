package com.blog.mapper;

import com.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    int insert(Comment comment);
    List<Comment> findByArticleIdAndStatus(@Param("articleId") Long articleId, @Param("status") String status);
    List<Comment> findAll();
    List<Comment> findAllWithStatus(@Param("status") String status);
    int countByStatus(@Param("status") String status);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int delete(@Param("id") Long id);
    int deleteByArticleId(@Param("articleId") Long articleId);
}
