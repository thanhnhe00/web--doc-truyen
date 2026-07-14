package com.storyreading.storyreadingbackend;

import com.storyreading.storyreadingbackend.dto.ChapterReadResponse;
import com.storyreading.storyreadingbackend.entity.Category;
import com.storyreading.storyreadingbackend.entity.Chapter;
import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.repository.CategoryRepository;
import com.storyreading.storyreadingbackend.repository.ChapterRepository;
import com.storyreading.storyreadingbackend.repository.StoryRepository;
import com.storyreading.storyreadingbackend.service.ReadingService;
import com.storyreading.storyreadingbackend.service.StorySearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class GuestModuleTests {

    @Autowired
    private StorySearchService storySearchService;

    @Autowired
    private ReadingService readingService;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testCombinedSearch() {
        Category tienHiep = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Tiên Hiệp"))
                .findFirst()
                .orElseThrow();

        Page<Story> results = storySearchService.search(
                "Nguyên",
                tienHiep.getCategoryId(),
                "Thiên Tàm Thổ Đậu",
                PageRequest.of(0, 10)
        );

        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        assertEquals("Nguyên Tôn", results.getContent().get(0).getTitle());
    }

    @Test
    void testGuestAgeRestriction_AgeRatingGreaterThanZero() {
        Story story = storyRepository.findAll().stream()
                .filter(s -> s.getTitle().equals("Toàn Trí Độc Giả"))
                .findFirst()
                .orElseThrow();

        List<Chapter> chapters = chapterRepository.findByStory_StoryIdAndStatusOrderByChapterNumberAsc(
                story.getStoryId(),
                com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus.PUBLISHED
        );
        assertFalse(chapters.isEmpty());
        Long chapterId = chapters.get(0).getChapterId();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            readingService.readChapter(chapterId, null);
        });

        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("yêu cầu xác minh độ tuổi"));
    }

    @Test
    void testGuestAgeRestriction_AgeRatingZero() {
        Story story = storyRepository.findAll().stream()
                .filter(s -> s.getTitle().equals("Võ Luyện Đỉnh Phong"))
                .findFirst()
                .orElseThrow();

        List<Chapter> chapters = chapterRepository.findByStory_StoryIdAndStatusOrderByChapterNumberAsc(
                story.getStoryId(),
                com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus.PUBLISHED
        );
        assertFalse(chapters.isEmpty());
        Long chapterId = chapters.get(0).getChapterId();

        ChapterReadResponse response = readingService.readChapter(chapterId, null);
        assertNotNull(response);
        assertEquals("Võ Luyện Đỉnh Phong", response.getStoryTitle());
    }
}
