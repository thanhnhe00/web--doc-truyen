package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.RatingRequest;
import com.storyreading.storyreadingbackend.dto.RatingSummaryResponse;
import com.storyreading.storyreadingbackend.entity.Rating;
import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.entity.User;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.repository.RatingRepository;
import com.storyreading.storyreadingbackend.repository.StoryRepository;
import com.storyreading.storyreadingbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;

    /** Tạo mới nếu chưa từng đánh giá, hoặc cập nhật lại điểm nếu đã đánh giá rồi (upsert). */
    public RatingSummaryResponse rate(Long storyId, RatingRequest request, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện"));

        if (story.getStatus() != ApprovalStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Truyện này chưa được xuất bản");
        }

        Rating rating = ratingRepository.findByUser_UserIdAndStory_StoryId(user.getUserId(), storyId)
                .orElseGet(() -> {
                    Rating r = new Rating();
                    r.setUser(user);
                    r.setStory(story);
                    return r;
                });
        rating.setScore(request.getScore());
        ratingRepository.save(rating);

        return buildSummary(storyId, rating.getScore());
    }

    public RatingSummaryResponse getSummary(Long storyId, Authentication authentication) {
        Short myScore = null;
        if (authentication != null) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                myScore = ratingRepository.findByUser_UserIdAndStory_StoryId(user.getUserId(), storyId)
                        .map(Rating::getScore)
                        .orElse(null);
            }
        }
        return buildSummary(storyId, myScore);
    }

    private RatingSummaryResponse buildSummary(Long storyId, Short myScore) {
        Double avg = ratingRepository.findAverageScoreByStoryId(storyId);
        long count = ratingRepository.countByStory_StoryId(storyId);
        double rounded = avg != null ? Math.round(avg * 10) / 10.0 : 0.0;
        return new RatingSummaryResponse(rounded, count, myScore);
    }
}
