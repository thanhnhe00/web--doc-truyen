package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByChapter_ChapterIdAndParentIsNull(Long chapterId, Pageable pageable);
    Page<Comment> findByChapter_ChapterIdAndParentIsNullAndIsHiddenFalse(Long chapterId, Pageable pageable);
    List<Comment> findByParent_CommentIdAndIsHiddenFalseOrderByCreatedAtAsc(Long parentId);
    List<Comment> findByParent_CommentIdOrderByCreatedAtAsc(Long parentId);
    long countByUser_UserIdAndCreatedAtAfter(Long userId, java.time.LocalDateTime after);
    long countByChapter_ChapterIdAndIsHiddenFalse(Long chapterId);
    long countByIsHiddenTrue();
    Page<Comment> findByIsHiddenTrue(Pageable pageable);
    List<Comment> findByChapter_ChapterIdOrderByCreatedAtDesc(Long chapterId);
    Optional<Comment> findFirstByUser_UserIdOrderByCreatedAtDesc(Long userId);
    long countByChapter_Story_StoryIdAndIsHiddenFalse(Long storyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.chapter.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.chapter.chapterId = :chapterId")
    void deleteByChapterId(Long chapterId);
}
