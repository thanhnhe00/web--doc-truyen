package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.ChapterSummaryResponse;
import com.storyreading.storyreadingbackend.dto.StoryDetailResponse;
import com.storyreading.storyreadingbackend.dto.StoryRequest;
import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.repository.StoryRepository;
import com.storyreading.storyreadingbackend.service.ChapterService;
import com.storyreading.storyreadingbackend.service.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final ChapterService chapterService;
    private final StoryRepository storyRepository;

    @PreAuthorize("hasRole('CREATOR')")
    @PostMapping
    public ResponseEntity<Story> create(@Valid @RequestBody StoryRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(storyService.create(request, authentication));
    }

    @PreAuthorize("hasRole('CREATOR')")
    @GetMapping("/my")
    public ResponseEntity<List<Story>> getMyStories(Authentication authentication) {
        return ResponseEntity.ok(storyService.getMyStories(authentication));
    }

    @PreAuthorize("hasRole('CREATOR')")
    @PatchMapping("/{id}/submit")
    public ResponseEntity<Story> submit(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(storyService.submitForApproval(id, authentication));
    }

    @PreAuthorize("hasRole('CREATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Story> update(@PathVariable Long id, @Valid @RequestBody StoryRequest request, Authentication authentication) {
        return ResponseEntity.ok(storyService.updateStory(id, request, authentication));
    }

    @PreAuthorize("hasRole('CREATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        storyService.deleteStory(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoryDetailResponse> getDetail(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(storyService.getStoryDetail(id, authentication));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Story>> getLatest(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(storyService.getLatestStories(limit));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Page<Story>> getByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(storyRepository.findByCategoryIdAndStatus(categoryId, ApprovalStatus.PUBLISHED, pageable));
    }

    @GetMapping("/{id}/chapters")
    public ResponseEntity<List<ChapterSummaryResponse>> getChapters(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(chapterService.getChaptersForStory(id, authentication));
    }

    @GetMapping("/by-author")
    public ResponseEntity<List<Story>> getByAuthor(@RequestParam String author) {
        return ResponseEntity.ok(storyRepository.findByAuthorContainingIgnoreCaseAndStatus(
                author, ApprovalStatus.PUBLISHED));
    }
}