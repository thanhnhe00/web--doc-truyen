package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.ChapterSummaryResponse;
import java.util.List;
import java.util.stream.Collectors;
import com.storyreading.storyreadingbackend.dto.ChapterRequest;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final StoryRepository storyRepository;
    private final ChapterImageRepository chapterImageRepository;
    private final CommentRepository commentRepository;
    private final ViewLogRepository viewLogRepository;
    private final HistoryRepository historyRepository;

    public Chapter create(Long storyId, ChapterRequest request, Authentication authentication) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy truyện"));

        if (!story.getCreator().getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền với truyện này");
        }

        if (chapterRepository.existsByStory_StoryIdAndChapterNumber(story.getStoryId(), request.getChapterNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Số chương đã tồn tại");
        }

        Chapter chapter = new Chapter();
        chapter.setStory(story);
        chapter.setChapterNumber(request.getChapterNumber());
        chapter.setTitle(request.getTitle());
        chapter.setContent(request.getContent());
        chapter.setStatus(ApprovalStatus.DRAFT);

        Chapter saved = chapterRepository.save(chapter);

        if (request.getImageUrls() != null) {
            int page = 1;
            for (String url : request.getImageUrls()) {
                ChapterImage img = new ChapterImage();
                img.setChapter(saved);
                img.setImageUrl(url);
                img.setPageNumber(page++);
                chapterImageRepository.save(img);
            }
        }

        return saved;
    }

    public List<ChapterSummaryResponse> getPublishedChapters(Long storyId) {
        return chapterRepository.findByStory_StoryIdAndStatusOrderByChapterNumberAsc(storyId, ApprovalStatus.PUBLISHED)
                .stream()
                .map(c -> new ChapterSummaryResponse(c.getChapterId(), c.getChapterNumber(), c.getTitle(), c.getCreatedAt()))
                .collect(Collectors.toList());
    }
    public Chapter submitForApproval(Long chapterId, Authentication authentication) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương"));

        if (!chapter.getStory().getCreator().getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền với chương này");
        }

        chapter.setStatus(ApprovalStatus.PENDING);
        return chapterRepository.save(chapter);
    }

    public Chapter updateChapter(Long chapterId, ChapterRequest request, Authentication authentication) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương"));

        if (!chapter.getStory().getCreator().getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền với chương này");
        }

        chapter.setTitle(request.getTitle());
        chapter.setContent(request.getContent());

        if (request.getImageUrls() != null) {
            chapterImageRepository.deleteByChapterId(chapterId);
            int page = 1;
            for (String url : request.getImageUrls()) {
                ChapterImage img = new ChapterImage();
                img.setChapter(chapter);
                img.setImageUrl(url);
                img.setPageNumber(page++);
                chapterImageRepository.save(img);
            }
        }

        return chapterRepository.save(chapter);
    }

    @Transactional
    public void deleteChapter(Long chapterId, Authentication authentication) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương"));

        if (!chapter.getStory().getCreator().getUsername().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền với chương này");
        }

        chapterImageRepository.deleteByChapterId(chapterId);
        commentRepository.deleteByChapterId(chapterId);
        viewLogRepository.deleteByChapterId(chapterId);
        historyRepository.deleteByChapterId(chapterId);
        chapterRepository.delete(chapter);
    }
}