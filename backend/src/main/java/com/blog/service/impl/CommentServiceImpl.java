package com.blog.service.impl;

import com.blog.dto.CommentRequest;
import com.blog.entity.Comment;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    @Transactional
    public void submitComment(Long articleId, CommentRequest request) {
        if (request.getHoneypot() != null && !request.getHoneypot().isEmpty()) {
            throw new SpamException();
        }
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setAuthorName(request.getAuthorName());
        comment.setContent(request.getContent());
        comment.setStatus("PENDING");
        commentMapper.insert(comment);
    }

    @Override
    public List<Comment> getApprovedComments(Long articleId) {
        return commentMapper.findByArticleIdAndStatus(articleId, "APPROVED");
    }

    @Override
    public List<Comment> getAllComments() {
        return commentMapper.findAll();
    }

    @Override
    public List<Comment> getCommentsByStatus(String status) {
        return commentMapper.findAllWithStatus(status);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        commentMapper.updateStatus(id, status);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        commentMapper.delete(id);
    }

    @Override
    public int countByStatus(String status) {
        return commentMapper.countByStatus(status);
    }
}
