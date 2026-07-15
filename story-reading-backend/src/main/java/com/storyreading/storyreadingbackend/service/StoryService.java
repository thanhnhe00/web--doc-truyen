package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.StoryDetailResponse;
import com.storyreading.storyreadingbackend.dto.StoryRequest;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.entity.enums.ContentType;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StoryCategoryRepository storyCategoryRepository;
    private final ViewLogRepository viewLogRepository;
    private final RatingRepository ratingRepository;
    private final FollowRepository followRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterImageRepository chapterImageRepository;
    private final CommentRepository commentRepository;
    private final HistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;
    private final CollectionStoryRepository collectionStoryRepository;

    public Story create(StoryRequest request, Authentication authentication) {
        User creator = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));

        Story story = new Story();
        story.setCreator(creator);
        story.setTitle(request.getTitle());
        story.setAuthor(request.getAuthor());
        story.setDescription(request.getDescription());
        story.setCoverImage(request.getCoverImage());
        story.setAgeRating(request.getAgeRating() != null ? request.getAgeRating() : 0);
        story.setContentType(ContentType.valueOf(request.getContentType()));
        story.setStatus(ApprovalStatus.DRAFT);

        Story saved = storyRepository.save(story);

        List<Category> cats = new java.util.ArrayList<>();
        if (request.getCategoryIds() != null) {
            for (Long catId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(catId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thể loại id=" + catId));
                StoryCategory sc = new StoryCategory();

                sc.setStory(saved);
                sc.setCategory(category);
                storyCategoryRepository.save(sc);
                cats.add(category);
            }
        }
        saved.setCategories(cats);

        return saved;
    }
    public Story getPublicDetail(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện"));
        if (story.getStatus() != ApprovalStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Truyện này chưa được xuất bản");
        }
        return story;
    }

    /** Chi tiết truyện đầy đủ cho trang StoryDetail (FR: xem chi tiết truyện). */
    public StoryDetailResponse getStoryDetail(Long storyId, Authentication authentication) {
        Story story = getPublicDetail(storyId);

        List<String> categories = storyCategoryRepository.findByStory_StoryId(storyId)
                .stream().map(sc -> sc.getCategory().getName()).collect(Collectors.toList());

        long viewCount = viewLogRepository.countByStory_StoryId(storyId);
        Double avg = ratingRepository.findAverageScoreByStoryId(storyId);
        long ratingCount = ratingRepository.countByStory_StoryId(storyId);
        long followerCount = followRepository.countByStory_StoryId(storyId);
        long commentCount = commentRepository.countByChapter_Story_StoryIdAndIsHiddenFalse(storyId);

        Boolean isFollowing = null;
        if (authentication != null) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                isFollowing = followRepository.existsByUser_UserIdAndStory_StoryId(user.getUserId(), storyId);
            }
        }

        return new StoryDetailResponse(
                story.getStoryId(),
                story.getTitle(),
                story.getAuthor(),
                story.getDescription(),
                story.getCoverImage(),
                story.getAgeRating(),
                story.getContentType().name(),
                story.getStatus().name(),
                story.getCreatedAt(),
                story.getCreator().getUsername(),
                categories,
                viewCount,
                avg != null ? Math.round(avg * 10) / 10.0 : 0.0,
                ratingCount,
                followerCount,
                commentCount,
                isFollowing
        );
    }

    public List<Story> getLatestStories(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return storyRepository.findByStatus(ApprovalStatus.PUBLISHED, pageable).getContent();
    }
    public List<Story> getMyStories(Authentication authentication) {
        User creator = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));
        List<Story> stories = storyRepository.findByCreator_UserId(creator.getUserId());
        for (Story s : stories) {
            List<Category> cats = storyCategoryRepository.findByStory_StoryId(s.getStoryId())
                    .stream()
                    .map(StoryCategory::getCategory)
                    .collect(Collectors.toList());
            s.setCategories(cats);
        }
        return stories;
    }

    public Story submitForApproval(Long storyId, Authentication authentication) {
        Story story = getOwnedStory(storyId, authentication);
        story.setStatus(ApprovalStatus.PENDING);
        List<Category> cats = storyCategoryRepository.findByStory_StoryId(storyId)
                .stream()
                .map(StoryCategory::getCategory)
                .collect(Collectors.toList());
        story.setCategories(cats);
        return storyRepository.save(story);
    }

    public Story updateStory(Long storyId, StoryRequest request, Authentication authentication) {
        Story story = getOwnedStory(storyId, authentication);
        story.setTitle(request.getTitle());
        story.setAuthor(request.getAuthor());
        story.setDescription(request.getDescription());
        story.setCoverImage(request.getCoverImage());
        story.setAgeRating(request.getAgeRating() != null ? request.getAgeRating() : story.getAgeRating());
        story.setContentType(ContentType.valueOf(request.getContentType()));

        List<Category> cats = new java.util.ArrayList<>();
        if (request.getCategoryIds() != null) {
            storyCategoryRepository.deleteByStoryId(storyId);
            for (Long catId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(catId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thể loại id=" + catId));
                StoryCategory sc = new StoryCategory();
                sc.setStory(story);
                sc.setCategory(category);
                storyCategoryRepository.save(sc);
                cats.add(category);
            }
        } else {
            cats = storyCategoryRepository.findByStory_StoryId(storyId)
                    .stream()
                    .map(StoryCategory::getCategory)
                    .collect(Collectors.toList());
        }
        story.setCategories(cats);

        return storyRepository.save(story);
    }

    @Transactional
    public void deleteStory(Long storyId, Authentication authentication) {
        Story story = getOwnedStory(storyId, authentication);
        if (story.getStatus() != ApprovalStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ có thể xóa truyện ở trạng thái bản nháp");
        }
        chapterImageRepository.deleteByStoryId(storyId);
        commentRepository.deleteByStoryId(storyId);
        viewLogRepository.deleteByStoryId(storyId);
        historyRepository.deleteByStoryId(storyId);
        ratingRepository.deleteByStoryId(storyId);
        followRepository.deleteByStoryId(storyId);
        notificationRepository.deleteByStoryId(storyId);
        collectionStoryRepository.deleteByStoryId(storyId);
        storyCategoryRepository.deleteByStoryId(storyId);
        chapterRepository.deleteByStoryId(storyId);
        storyRepository.delete(story);
    }

    private Story getOwnedStory(Long storyId, Authentication authentication) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện"));
        if (!story.getCreator().getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền với truyện này");
        }
        return story;
    }
}