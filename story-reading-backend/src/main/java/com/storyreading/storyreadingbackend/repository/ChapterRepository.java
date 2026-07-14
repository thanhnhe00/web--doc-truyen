package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.Chapter;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByStory_StoryIdOrderByChapterNumberAsc(Long storyId);
    Optional<Chapter> findByStory_StoryIdAndChapterNumber(Long storyId, Integer chapterNumber);
    boolean existsByStory_StoryIdAndChapterNumber(Long storyId, Integer chapterNumber);
    List<Chapter> findByStatus(ApprovalStatus status);
    List<Chapter> findByStory_StoryIdAndStatusOrderByChapterNumberAsc(Long storyId, ApprovalStatus status);
    long countByStory_StoryIdAndStatus(Long storyId, ApprovalStatus status);
    long countByStory_Creator_UserIdAndStatus(Long creatorId, ApprovalStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Chapter c WHERE c.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);
}
