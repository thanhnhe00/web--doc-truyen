package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.ThemeCollectionRequest;
import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.entity.ThemeCollection;
import com.storyreading.storyreadingbackend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiscoveryController {

    private final TrendingService trendingService;
    private final RecommendationService recommendationService;
    private final ThemeCollectionService themeCollectionService;

    @GetMapping("/api/stories/trending")
    public ResponseEntity<List<Story>> trending(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(trendingService.getTrending(days, limit));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/stories/recommendations")
    public ResponseEntity<List<Story>> recommendations(
            @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return ResponseEntity.ok(recommendationService.getRecommendations(authentication, limit));
    }

    @GetMapping("/api/collections")
    public ResponseEntity<List<ThemeCollection>> getCollections() {
        return ResponseEntity.ok(themeCollectionService.getAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/collections")
    public ResponseEntity<ThemeCollection> createCollection(@Valid @RequestBody ThemeCollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeCollectionService.create(request));
    }

    @GetMapping("/api/collections/{id}/stories")
    public ResponseEntity<List<Story>> getCollectionStories(@PathVariable Long id) {
        return ResponseEntity.ok(themeCollectionService.getStoriesByCollection(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/collections/{id}")
    public ResponseEntity<ThemeCollection> updateCollection(@PathVariable Long id, @Valid @RequestBody ThemeCollectionRequest request) {
        return ResponseEntity.ok(themeCollectionService.update(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/collections/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        themeCollectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}