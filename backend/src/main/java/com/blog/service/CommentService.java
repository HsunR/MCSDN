package com.blog.service;

import com.blog.dto.CommentRequest;
import com.blog.entity.Comment;

import java.util.List;

public interface CommentService {
    void submitComment(Long articleId, CommentRequest request);
    List<Comment> getApprovedComments(Long articleId);
    List<Comment> getAllComments();
    List<Comment> getCommentsByStatus(String status);
    void updateStatus(Long id, String status);
    void deleteComment(Long id);
    int countByStatus(String status);
}
