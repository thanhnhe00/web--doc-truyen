package com.storyreading.storyreadingbackend.controller;

import com.storyreading.storyreadingbackend.dto.*;
import com.storyreading.storyreadingbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(authentication));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                               Authentication authentication) {
        return ResponseEntity.ok(userService.updateProfile(request, authentication));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                Authentication authentication) {
        userService.changePassword(request, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<Page<HistoryResponse>> getHistory(Authentication authentication, Pageable pageable) {
        return ResponseEntity.ok(userService.getHistory(authentication, pageable));
    }

    @GetMapping("/following")
    public ResponseEntity<List<FollowingResponse>> getFollowing(Authentication authentication) {
        return ResponseEntity.ok(userService.getFollowing(authentication));
    }

    @GetMapping("/history/{storyId}")
    public ResponseEntity<HistoryResponse> getResumeReading(@PathVariable Long storyId, Authentication authentication) {
        HistoryResponse response = userService.getResumeReading(storyId, authentication);
        if (response == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/history/{storyId}/prompted")
    public ResponseEntity<Void> markHistoryPrompted(@PathVariable Long storyId, Authentication authentication) {
        userService.markHistoryPrompted(storyId, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/creator/stats")
    public ResponseEntity<java.util.Map<String, Object>> getCreatorStats(Authentication authentication) {
        return ResponseEntity.ok(userService.getCreatorStats(authentication));
    }

    @GetMapping("/creator/story-stats")
    public ResponseEntity<List<java.util.Map<String, Object>>> getCreatorStoryStats(Authentication authentication) {
        return ResponseEntity.ok(userService.getCreatorStoryStats(authentication));
    }
}
