package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByUser_UserIdAndStory_StoryId(Long userId, Long storyId);
    List<Follow> findByStory_StoryId(Long storyId);
    List<Follow> findByUser_UserId(Long userId);
    boolean existsByUser_UserIdAndStory_StoryId(Long userId, Long storyId);
    void deleteByUser_UserIdAndStory_StoryId(Long userId, Long storyId);
    long countByStory_StoryId(Long storyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Follow f WHERE f.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.story.creator.userId = :creatorId")
    long countByCreator_UserId(@Param("creatorId") Long creatorId);
}
