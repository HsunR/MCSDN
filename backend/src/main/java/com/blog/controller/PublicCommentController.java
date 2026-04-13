package com.blog.controller;

import com.blog.dto.CommentRequest;
import com.blog.entity.Comment;
import com.blog.service.CommentService;
import com.blog.service.impl.SpamException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
public class PublicCommentController {

    private final CommentService commentService;

    public PublicCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getApprovedComments(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> submitComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request) {
        try {
            commentService.submitComment(id, request);
            return ResponseEntity.status(201)
                .body(Map.of("message", "Comment submitted, awaiting approval."));
        } catch (SpamException e) {
            return ResponseEntity.ok()
                .body(Map.of("message", "Comment submitted, awaiting approval."));
        }
    }
}
