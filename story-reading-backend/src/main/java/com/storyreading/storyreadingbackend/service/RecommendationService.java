package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final StoryCategoryRepository storyCategoryRepository;
    private final StoryRepository storyRepository;
    private final HistoryRepository historyRepository;

    public List<Story> getRecommendations(Authentication authentication, int limit) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));

        List<Story> followedStories = followRepository.findByUser_UserId(user.getUserId())
                .stream().map(Follow::getStory).collect(Collectors.toList());

        // Thu thập thể loại yêu thích từ truyện đã theo dõi
        Set<Long> favoriteCategoryIds = followedStories.stream()
                .flatMap(s -> storyCategoryRepository.findByStory_StoryId(s.getStoryId()).stream())
                .map(sc -> sc.getCategory().getCategoryId())
                .collect(Collectors.toSet());

        // Thu thập thể loại từ lịch sử đọc
        List<History> histories = historyRepository.findByUser_UserIdOrderByUpdatedAtDesc(user.getUserId(), PageRequest.of(0, 20)).getContent();
        for (History h : histories) {
            storyCategoryRepository.findByStory_StoryId(h.getStory().getStoryId()).stream()
                    .map(sc -> sc.getCategory().getCategoryId())
                    .forEach(favoriteCategoryIds::add);
        }

        Set<Long> followedIds = followedStories.stream().map(Story::getStoryId).collect(Collectors.toSet());

        // Tính tuổi người dùng để lọc theo độ tuổi
        int userAge = calculateAge(user);

        // Lấy danh sách ID truyện đã đọc
        Set<Long> readStoryIds = histories.stream()
                .map(h -> h.getStory().getStoryId())
                .collect(Collectors.toSet());

        if (favoriteCategoryIds.isEmpty()) {
            return filterByAge(storyRepository.findByStatus(ApprovalStatus.PUBLISHED, PageRequest.of(0, limit * 2)).getContent(), userAge, followedIds, readStoryIds)
                    .stream().limit(limit).collect(Collectors.toList());
        }

        // Ưu tiên: thể loại yêu thích → chưa đọc → chưa theo dõi → phù hợp độ tuổi
        List<Story> candidates = favoriteCategoryIds.stream()
                .flatMap(catId -> storyCategoryRepository.findByCategory_CategoryId(catId).stream())
                .map(StoryCategory::getStory)
                .filter(s -> s.getStatus() == ApprovalStatus.PUBLISHED)
                .filter(s -> !followedIds.contains(s.getStoryId()))
                .filter(s -> !readStoryIds.contains(s.getStoryId()))
                .filter(s -> isAgeAppropriate(s, userAge))
                .distinct()
                .collect(Collectors.toList());

        // Nếu chưa đủ, bổ sung truyện mới từ thể loại yêu thích (bao gồm cả đã đọc)
        if (candidates.size() < limit) {
            List<Story> extra = favoriteCategoryIds.stream()
                    .flatMap(catId -> storyCategoryRepository.findByCategory_CategoryId(catId).stream())
                    .map(StoryCategory::getStory)
                    .filter(s -> s.getStatus() == ApprovalStatus.PUBLISHED)
                    .filter(s -> !followedIds.contains(s.getStoryId()))
                    .filter(s -> isAgeAppropriate(s, userAge))
                    .distinct()
                    .toList();
            candidates.addAll(extra);
        }

        // Nếu vẫn chưa đủ, thêm truyện phổ biến
        if (candidates.size() < limit) {
            List<Story> popular = storyRepository.findByStatus(ApprovalStatus.PUBLISHED, PageRequest.of(0, limit))
                    .stream()
                    .filter(s -> !followedIds.contains(s.getStoryId()))
                    .filter(s -> isAgeAppropriate(s, userAge))
                    .toList();
            candidates.addAll(popular);
        }

        return candidates.stream().distinct().limit(limit).collect(Collectors.toList());
    }

    private int calculateAge(User user) {
        if (user.getBirthDate() == null) return 99;
        return Period.between(user.getBirthDate(), LocalDate.now()).getYears();
    }

    private boolean isAgeAppropriate(Story story, int userAge) {
        int rating = story.getAgeRating() != null ? story.getAgeRating() : 0;
        return rating == 0 || userAge >= rating;
    }

    private List<Story> filterByAge(List<Story> stories, int userAge, Set<Long> followedIds, Set<Long> readStoryIds) {
        return stories.stream()
                .filter(s -> !followedIds.contains(s.getStoryId()))
                .filter(s -> !readStoryIds.contains(s.getStoryId()))
                .filter(s -> isAgeAppropriate(s, userAge))
                .collect(Collectors.toList());
    }
}