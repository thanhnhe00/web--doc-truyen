package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.*;
import com.storyreading.storyreadingbackend.entity.Follow;
import com.storyreading.storyreadingbackend.entity.History;
import com.storyreading.storyreadingbackend.entity.User;
import com.storyreading.storyreadingbackend.entity.enums.UserRole;
import com.storyreading.storyreadingbackend.repository.FollowRepository;
import com.storyreading.storyreadingbackend.repository.HistoryRepository;
import com.storyreading.storyreadingbackend.repository.ReportRepository;
import com.storyreading.storyreadingbackend.repository.StoryRepository;
import com.storyreading.storyreadingbackend.repository.UserRepository;
import com.storyreading.storyreadingbackend.repository.ViewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HistoryRepository historyRepository;
    private final FollowRepository followRepository;
    private final StoryRepository storyRepository;
    private final ReportRepository reportRepository;
    private final ViewLogRepository viewLogRepository;

    public UserProfileResponse getProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(UpdateProfileRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }

        return toProfileResponse(userRepository.save(user));
    }

    public void changePassword(ChangePasswordRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public Page<HistoryResponse> getHistory(Authentication authentication, Pageable pageable) {
        User user = getCurrentUser(authentication);
        return historyRepository.findByUser_UserIdOrderByUpdatedAtDesc(user.getUserId(), pageable)
                .map(this::toHistoryResponse);
    }

    public List<FollowingResponse> getFollowing(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return followRepository.findByUser_UserId(user.getUserId())
                .stream()
                .map(this::toFollowingResponse)
                .toList();
    }

    private HistoryResponse toHistoryResponse(History h) {
        return new HistoryResponse(
                h.getStory().getStoryId(),
                h.getStory().getTitle(),
                h.getStory().getCoverImage(),
                h.getChapter().getChapterId(),
                h.getChapter().getChapterNumber(),
                h.getChapter().getTitle(),
                h.getUpdatedAt(),
                h.getIsPrompted()
        );
    }

    private FollowingResponse toFollowingResponse(Follow f) {
        return new FollowingResponse(
                f.getStory().getStoryId(),
                f.getStory().getTitle(),
                f.getStory().getAuthor(),
                f.getStory().getCoverImage(),
                f.getStory().getStatus().name(),
                f.getStory().getContentType().name()
        );
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getBirthDate(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));
    }

    public HistoryResponse getResumeReading(Long storyId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return historyRepository.findByUser_UserIdAndStory_StoryId(user.getUserId(), storyId)
                .map(this::toHistoryResponse)
                .orElse(null);
    }

    public void markHistoryPrompted(Long storyId, Authentication authentication) {
        User user = getCurrentUser(authentication);
        historyRepository.findByUser_UserIdAndStory_StoryId(user.getUserId(), storyId)
                .ifPresent(h -> {
                    h.setIsPrompted(true);
                    historyRepository.save(h);
                });
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Map<String, Object> getCreatorStats(Authentication authentication) {
        User creator = getCurrentUser(authentication);
        Long creatorId = creator.getUserId();

        long totalStories = storyRepository.findByCreator_UserId(creatorId).size();
        long totalViews = viewLogRepository.countByCreator_UserId(creatorId);
        long totalFollowers = followRepository.countByCreator_UserId(creatorId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStories", totalStories);
        stats.put("totalViews", totalViews);
        stats.put("totalFollowers", totalFollowers);
        return stats;
    }

    public User changeRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        user.setRole(role);
        return userRepository.save(user);
    }

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalStories", storyRepository.count());
        stats.put("pendingReports", reportRepository.findByStatus(
                com.storyreading.storyreadingbackend.entity.enums.ReportStatus.PENDING).size());
        return stats;
    }

    public List<Map<String, Object>> getCreatorStoryStats(Authentication authentication) {
        User creator = getCurrentUser(authentication);
        Long creatorId = creator.getUserId();

        List<com.storyreading.storyreadingbackend.entity.Story> stories = storyRepository.findByCreator_UserId(creatorId);

        return stories.stream().map(story -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("storyId", story.getStoryId());
            stat.put("title", story.getTitle());
            stat.put("status", story.getStatus().name());
            stat.put("viewCount", viewLogRepository.countByStory_StoryId(story.getStoryId()));
            stat.put("followerCount", followRepository.countByStory_StoryId(story.getStoryId()));
            return stat;
        }).toList();
    }
}
