package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.entity.ViewLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {
    List<ViewLog> findByStory_StoryIdAndViewedAtAfter(Long storyId, LocalDateTime after);
    long countByStory_StoryId(Long storyId);

    @Query("""
        SELECT v.story FROM ViewLog v
        WHERE v.viewedAt >= :since
        GROUP BY v.story
        ORDER BY COUNT(v.viewLogId) DESC
        """)
    List<Story> findTrendingStories(@Param("since") LocalDateTime since, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM ViewLog v WHERE v.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ViewLog v WHERE v.chapter.chapterId = :chapterId")
    void deleteByChapterId(Long chapterId);

    @Query("SELECT COUNT(v) FROM ViewLog v WHERE v.story.creator.userId = :creatorId")
    long countByCreator_UserId(@Param("creatorId") Long creatorId);
}
