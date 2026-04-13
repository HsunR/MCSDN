package com.blog.controller;

import com.blog.entity.Comment;
import com.blog.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getAllComments(
            @RequestParam(required = false) String status) {
        if (status == null) {
            return ResponseEntity.ok(commentService.getAllComments());
        }
        return ResponseEntity.ok(commentService.getCommentsByStatus(status));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveComment(@PathVariable Long id) {
        commentService.updateStatus(id, "APPROVED");
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectComment(@PathVariable Long id) {
        commentService.updateStatus(id, "REJECTED");
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
