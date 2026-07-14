package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Long> {
    Optional<History> findByUser_UserIdAndStory_StoryId(Long userId, Long storyId);
    Page<History> findByUser_UserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM History h WHERE h.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM History h WHERE h.chapter.chapterId = :chapterId")
    void deleteByChapterId(Long chapterId);
}
