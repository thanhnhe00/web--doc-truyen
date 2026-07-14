package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.ChapterReadResponse;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final ChapterRepository chapterRepository;
    private final ChapterImageRepository chapterImageRepository;
    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ViewLogRepository viewLogRepository;

    public ChapterReadResponse readChapter(Long chapterId, Authentication authentication) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương"));

        if (chapter.getStatus() != ApprovalStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chương này chưa được xuất bản");
        }

        // FR17: Kiểm tra độ tuổi
        int ageRating = chapter.getStory().getAgeRating() != null ? chapter.getStory().getAgeRating() : 0;
        if (ageRating > 0) {
            if (authentication == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Nội dung này yêu cầu xác minh độ tuổi. Vui lòng đăng nhập.");
            }
            User ageUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (ageUser != null && ageUser.getBirthDate() != null) {
                int userAge = Period.between(ageUser.getBirthDate(), LocalDate.now()).getYears();
                if (userAge < ageRating) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Nội dung này yêu cầu độ tuổi " + ageRating + " trở lên. Bạn chỉ mới " + userAge + " tuổi.");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Vui lòng cập nhật ngày sinh để xác minh độ tuổi.");
            }
        }

        ViewLog log = new ViewLog();
        log.setStory(chapter.getStory());
        log.setChapter(chapter);

        User user = null;
        if (authentication != null) {
            user = userRepository.findByUsername(authentication.getName()).orElse(null);
            log.setUser(user);
        }
        viewLogRepository.save(log);

        if (user != null) {
            saveHistory(chapter, user);
        }

        List<String> imageUrls = "COMIC".equals(chapter.getStory().getContentType().name())
                ? chapterImageRepository.findByChapter_ChapterIdOrderByPageNumberAsc(chapterId)
                .stream().map(ChapterImage::getImageUrl).collect(Collectors.toList())
                : Collections.emptyList();

        Long storyId = chapter.getStory().getStoryId();
        Long prevChapterId = chapterRepository
                .findByStory_StoryIdAndChapterNumber(storyId, chapter.getChapterNumber() - 1)
                .filter(c -> c.getStatus() == ApprovalStatus.PUBLISHED)
                .map(Chapter::getChapterId)
                .orElse(null);
        Long nextChapterId = chapterRepository
                .findByStory_StoryIdAndChapterNumber(storyId, chapter.getChapterNumber() + 1)
                .filter(c -> c.getStatus() == ApprovalStatus.PUBLISHED)
                .map(Chapter::getChapterId)
                .orElse(null);

        return new ChapterReadResponse(
                chapter.getChapterId(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                chapter.getContent(),
                imageUrls,
                storyId,
                chapter.getStory().getTitle(),
                chapter.getStory().getContentType().name(),
                prevChapterId,
                nextChapterId
        );
    }

    private void saveHistory(Chapter chapter, User user) {
        History history = historyRepository.findByUser_UserIdAndStory_StoryId(user.getUserId(), chapter.getStory().getStoryId())
                .orElseGet(() -> {
                    History h = new History();
                    h.setUser(user);
                    h.setStory(chapter.getStory());
                    h.setIsPrompted(false);
                    return h;
                });

        // FR08: Nếu chapter hiện tại bị ẩn/rejected, tìm chapter hợp lệ gần nhất
        Chapter currentChapter = chapter;
        if (chapter.getStatus() != ApprovalStatus.PUBLISHED) {
            currentChapter = chapterRepository.findByStory_StoryIdAndStatusOrderByChapterNumberAsc(chapter.getStory().getStoryId(), ApprovalStatus.PUBLISHED)
                    .stream()
                    .filter(c -> c.getChapterNumber() <= chapter.getChapterNumber())
                    .reduce((a, b) -> b)
                    .orElse(chapterRepository.findByStory_StoryIdAndStatusOrderByChapterNumberAsc(chapter.getStory().getStoryId(), ApprovalStatus.PUBLISHED)
                            .stream().findFirst().orElse(chapter));
        }

        history.setChapter(currentChapter);
        history.setScrollPosition(0);
        historyRepository.save(history);
    }
}