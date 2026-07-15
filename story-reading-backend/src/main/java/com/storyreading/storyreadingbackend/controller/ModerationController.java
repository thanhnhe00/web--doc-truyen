package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.LockUserRequest;
import com.storyreading.storyreadingbackend.dto.ModerationRequest;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.ReportStatus;
import com.storyreading.storyreadingbackend.entity.enums.UserRole;
import com.storyreading.storyreadingbackend.service.ModerationService;
import com.storyreading.storyreadingbackend.service.ReportService;
import com.storyreading.storyreadingbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ModerationController {

    private final ModerationService moderationService;
    private final ReportService reportService;
    private final UserService userService;
    private final com.storyreading.storyreadingbackend.service.CommentService commentService;

    @GetMapping("/comments/{id}")
    public ResponseEntity<com.storyreading.storyreadingbackend.dto.CommentResponse> getCommentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentDetails(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(userService.searchUsers(search));
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<User> changeRole(@PathVariable Long id, @RequestParam UserRole role) {
        return ResponseEntity.ok(userService.changeRole(id, role));
    }

    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getStats() {
        return ResponseEntity.ok(userService.getAdminStats());
    }

    @PatchMapping("/stories/{id}/approve")
    public ResponseEntity<Story> approveStory(@PathVariable Long id) {
        return ResponseEntity.ok(moderationService.approveStory(id));
    }

    @PatchMapping("/stories/{id}/reject")
    public ResponseEntity<Story> rejectStory(@PathVariable Long id, @RequestBody ModerationRequest request) {
        return ResponseEntity.ok(moderationService.rejectStory(id, request));
    }

    @PatchMapping("/stories/{id}/hide")
    public ResponseEntity<Story> hideStory(@PathVariable Long id, @RequestBody ModerationRequest request) {
        return ResponseEntity.ok(moderationService.hideStory(id, request));
    }

    @PatchMapping("/chapters/{id}/approve")
    public ResponseEntity<Chapter> approveChapter(@PathVariable Long id) {
        return ResponseEntity.ok(moderationService.approveChapter(id));
    }

    @PatchMapping("/chapters/{id}/reject")
    public ResponseEntity<Chapter> rejectChapter(@PathVariable Long id, @RequestBody ModerationRequest request) {
        return ResponseEntity.ok(moderationService.rejectChapter(id, request));
    }

    @PatchMapping("/chapters/{id}/hide")
    public ResponseEntity<Chapter> hideChapter(@PathVariable Long id, @RequestBody ModerationRequest request) {
        return ResponseEntity.ok(moderationService.hideChapter(id, request));
    }

    @PatchMapping("/users/{id}/lock")
    public ResponseEntity<User> lockUser(@PathVariable Long id, @Valid @RequestBody LockUserRequest request) {
        return ResponseEntity.ok(moderationService.lockUser(id, request.getReason()));
    }

    @PatchMapping("/users/{id}/unlock")
    public ResponseEntity<User> unlockUser(@PathVariable Long id) {
        return ResponseEntity.ok(moderationService.unlockUser(id));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getPendingReports() {
        return ResponseEntity.ok(reportService.getPending());
    }

    @GetMapping("/chapters/pending")
    public ResponseEntity<List<Chapter>> getPendingChapters() {
        return ResponseEntity.ok(moderationService.getPendingChapters());
    }

    @GetMapping("/stories/pending")
    public ResponseEntity<List<Story>> getPendingStories() {
        return ResponseEntity.ok(moderationService.getPendingStories());
    }

    @PatchMapping("/reports/{id}/resolve")
    public ResponseEntity<Report> resolveReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.resolve(id, ReportStatus.RESOLVED));
    }

    @PatchMapping("/reports/{id}/dismiss")
    public ResponseEntity<Report> dismissReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.resolve(id, ReportStatus.DISMISSED));
    }
}