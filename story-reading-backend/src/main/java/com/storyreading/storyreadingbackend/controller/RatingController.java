package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.RatingRequest;
import com.storyreading.storyreadingbackend.dto.RatingSummaryResponse;
import com.storyreading.storyreadingbackend.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stories/{storyId}/rating")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @GetMapping
    public ResponseEntity<RatingSummaryResponse> getSummary(@PathVariable Long storyId, Authentication authentication) {
        return ResponseEntity.ok(ratingService.getSummary(storyId, authentication));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<RatingSummaryResponse> rate(@PathVariable Long storyId,
                                                        @Valid @RequestBody RatingRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(ratingService.rate(storyId, request, authentication));
    }
}
