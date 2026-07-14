package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.ChapterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChapterImageRepository extends JpaRepository<ChapterImage, Long> {
    List<ChapterImage> findByChapter_ChapterIdOrderByPageNumberAsc(Long chapterId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChapterImage ci WHERE ci.chapter.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChapterImage ci WHERE ci.chapter.chapterId = :chapterId")
    void deleteByChapterId(Long chapterId);
}
