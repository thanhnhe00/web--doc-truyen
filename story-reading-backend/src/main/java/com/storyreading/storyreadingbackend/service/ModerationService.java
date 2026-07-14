package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.ModerationRequest;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.*;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final StoryRepository storyRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FollowRepository followRepository;

    public Story approveStory(Long storyId) {
        Story story = getStory(storyId);
        story.setStatus(ApprovalStatus.PUBLISHED);
        notify(story.getCreator(), NotificationType.STORY_APPROVED,
                "Truyện đã được duyệt", "Truyện \"" + story.getTitle() + "\" đã được duyệt và xuất bản.");
        return storyRepository.save(story);
    }

    public Story rejectStory(Long storyId, ModerationRequest request) {
        Story story = getStory(storyId);
        story.setStatus(ApprovalStatus.REJECTED);
        story.setRejectionReason(request.getReason());
        notify(story.getCreator(), NotificationType.STORY_REJECTED,
                "Truyện bị từ chối", "Truyện \"" + story.getTitle() + "\" bị từ chối: " + request.getReason());
        return storyRepository.save(story);
    }

    public Story hideStory(Long storyId, ModerationRequest request) {
        Story story = getStory(storyId);
        story.setStatus(ApprovalStatus.HIDDEN);
        notify(story.getCreator(), NotificationType.CONTENT_HIDDEN,
                "Truyện đã bị ẩn", "Truyện \"" + story.getTitle() + "\" đã bị ẩn: " + request.getReason());
        return storyRepository.save(story);
    }

    public Chapter approveChapter(Long chapterId) {
        Chapter chapter = getChapter(chapterId);
        chapter.setStatus(ApprovalStatus.PUBLISHED);
        notify(chapter.getStory().getCreator(), NotificationType.CHAPTER_APPROVED,
                "Chương đã được duyệt", "Chương " + chapter.getChapterNumber() + " đã được duyệt.");

        List<Follow> followers = followRepository.findByStory_StoryId(chapter.getStory().getStoryId());
        for (Follow follow : followers) {
            if (!follow.getUser().getUserId().equals(chapter.getStory().getCreator().getUserId())) {
                Notification noti = new Notification();
                noti.setReceiver(follow.getUser());
                noti.setStory(chapter.getStory());
                noti.setType(NotificationType.NEW_CHAPTER);
                noti.setTitle("Chương mới");
                noti.setContent("Truyện \"" + chapter.getStory().getTitle() + "\" có chương mới: " + chapter.getTitle());
                noti.setIsRead(false);
                notificationRepository.save(noti);
            }
        }

        return chapterRepository.save(chapter);
    }

    public Chapter rejectChapter(Long chapterId, ModerationRequest request) {
        Chapter chapter = getChapter(chapterId);
        chapter.setStatus(ApprovalStatus.REJECTED);
        chapter.setRejectionReason(request.getReason());
        notify(chapter.getStory().getCreator(), NotificationType.CHAPTER_REJECTED,
                "Chương bị từ chối", "Chương " + chapter.getChapterNumber() + " bị từ chối: " + request.getReason());
        return chapterRepository.save(chapter);
    }

    public Chapter hideChapter(Long chapterId, ModerationRequest request) {
        Chapter chapter = getChapter(chapterId);
        chapter.setStatus(ApprovalStatus.HIDDEN);
        notify(chapter.getStory().getCreator(), NotificationType.CONTENT_HIDDEN,
                "Chương đã bị ẩn", "Chương " + chapter.getChapterNumber() + " đã bị ẩn: " + request.getReason());
        return chapterRepository.save(chapter);
    }

    public User lockUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        user.setStatus(UserStatus.LOCKED);
        user.setLockReason(reason);
        return userRepository.save(user);
    }

    public User unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        user.setStatus(UserStatus.ACTIVE);
        user.setLockReason(null);
        return userRepository.save(user);
    }

    public List<Chapter> getPendingChapters() {
        return chapterRepository.findByStatus(ApprovalStatus.PENDING);
    }

    public List<Story> getPendingStories() {
        return storyRepository.findByStatus(ApprovalStatus.PENDING);
    }

    private Story getStory(Long id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện"));
    }

    private Chapter getChapter(Long id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương"));
    }

    private void notify(User receiver, NotificationType type, String title, String content) {
        Notification noti = new Notification();
        noti.setReceiver(receiver);
        noti.setType(type);
        noti.setTitle(title);
        noti.setContent(content);
        noti.setIsRead(false);
        notificationRepository.save(noti);
    }
}