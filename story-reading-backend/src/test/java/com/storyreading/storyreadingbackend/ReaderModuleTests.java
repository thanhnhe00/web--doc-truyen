package com.storyreading.storyreadingbackend;

import com.storyreading.storyreadingbackend.dto.ReportRequest;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.repository.*;
import com.storyreading.storyreadingbackend.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReaderModuleTests {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReadingService readingService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Test
    void testDuplicateReportThrowsConflict() {
        // Prepare user and story
        User reporter = userRepository.findAll().get(0);
        Story story = storyRepository.findAll().get(0);

        Authentication auth = new UsernamePasswordAuthenticationToken(reporter.getUsername(), null);

        ReportRequest req1 = new ReportRequest();
        req1.setTargetType("STORY");
        req1.setTargetId(story.getStoryId());
        req1.setReason("Spam content");

        // First report should succeed
        assertNotNull(reportService.create(req1, auth));

        // Second report should throw CONFLICT (409)
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            reportService.create(req1, auth);
        });
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void testReadingResetIsPrompted() {
        // Prepare user, story and chapter
        User user = userRepository.findAll().get(0);
        Story story = storyRepository.findAll().get(0);
        List<Chapter> chapters = chapterRepository.findByStory_StoryIdAndStatusOrderByChapterNumberAsc(
                story.getStoryId(), ApprovalStatus.PUBLISHED);
        assertFalse(chapters.isEmpty());
        Chapter chapter = chapters.get(0);

        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null);

        // First read: creates history and isPrompted is false
        readingService.readChapter(chapter.getChapterId(), auth);
        History h = historyRepository.findByUser_UserIdAndStory_StoryId(user.getUserId(), story.getStoryId()).orElseThrow();
        assertFalse(h.getIsPrompted());

        // Set isPrompted to true manually simulating prompt
        h.setIsPrompted(true);
        historyRepository.save(h);

        // Read again: should reset isPrompted to false
        readingService.readChapter(chapter.getChapterId(), auth);
        History hUpdated = historyRepository.findByUser_UserIdAndStory_StoryId(user.getUserId(), story.getStoryId()).orElseThrow();
        assertFalse(hUpdated.getIsPrompted());
    }

    @Test
    void testRecommendationRetrieval() {
        User user = userRepository.findAll().get(0);
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null);

        List<Story> recommendations = recommendationService.getRecommendations(auth, 5);
        assertNotNull(recommendations);
        assertTrue(recommendations.size() <= 5);
    }
}
