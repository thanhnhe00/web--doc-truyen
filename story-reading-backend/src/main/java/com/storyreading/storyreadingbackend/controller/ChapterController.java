package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.ChapterRequest;
import com.storyreading.storyreadingbackend.entity.Chapter;
import com.storyreading.storyreadingbackend.service.ChapterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stories/{storyId}/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @PreAuthorize("hasRole('CREATOR')")
    @PostMapping
    public ResponseEntity<Chapter> create(@PathVariable Long storyId, @Valid @RequestBody ChapterRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.create(storyId, request, authentication));
    }

    @PreAuthorize("hasRole('CREATOR')")
    @PatchMapping("/{id}/submit")
    public ResponseEntity<Chapter> submit(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(chapterService.submitForApproval(id, authentication));
    }

    @PreAuthorize("hasRole('CREATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Chapter> update(@PathVariable Long id, @Valid @RequestBody ChapterRequest request, Authentication authentication) {
        return ResponseEntity.ok(chapterService.updateChapter(id, request, authentication));
    }

    @PreAuthorize("hasRole('CREATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        chapterService.deleteChapter(id, authentication);
        return ResponseEntity.noContent().build();
    }
}