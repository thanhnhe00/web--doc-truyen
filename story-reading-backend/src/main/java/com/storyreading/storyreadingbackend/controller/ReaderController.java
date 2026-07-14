package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.ChapterReadResponse;
import com.storyreading.storyreadingbackend.dto.CommentRequest;
import com.storyreading.storyreadingbackend.dto.CommentResponse;
import com.storyreading.storyreadingbackend.dto.CommentUpdateRequest;
import com.storyreading.storyreadingbackend.entity.Comment;
import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReaderController {

    private final StorySearchService storySearchService;
    private final ReadingService readingService;
    private final FollowService followService;
    private final CommentService commentService;

    @GetMapping("/api/stories/search")
    public ResponseEntity<Page<Story>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String author,
            Pageable pageable) {
        return ResponseEntity.ok(storySearchService.search(keyword, categoryId, author, pageable));
    }

    @GetMapping("/api/chapters/{id}/read")
    public ResponseEntity<ChapterReadResponse> read(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(readingService.readChapter(id, authentication));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/stories/{id}/follow")
    public ResponseEntity<Void> follow(@PathVariable Long id, Authentication authentication) {
        followService.follow(id, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/api/stories/{id}/follow")
    public ResponseEntity<Void> unfollow(@PathVariable Long id, Authentication authentication) {
        followService.unfollow(id, authentication);
        return ResponseEntity.noContent().build();
    }

    // ========== FR07: Bình luận & Phản hồi ==========

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/chapters/{id}/comments")
    public ResponseEntity<Comment> comment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(id, request, authentication));
    }

    @GetMapping("/api/chapters/{id}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean showAll,
            Pageable pageable) {
        if (showAll) {
            return ResponseEntity.ok(commentService.getByChapterAdmin(id, pageable));
        }
        return ResponseEntity.ok(commentService.getByChapter(id, pageable));
    }

    @GetMapping("/api/comments/{id}/replies")
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getReplies(id));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/api/comments/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.update(id, request, authentication));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            Authentication authentication) {
        commentService.delete(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/comments/{id}/hide")
    public ResponseEntity<Comment> hideComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.hide(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/comments/{id}/unhide")
    public ResponseEntity<Comment> unhideComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.unhide(id));
    }
}
