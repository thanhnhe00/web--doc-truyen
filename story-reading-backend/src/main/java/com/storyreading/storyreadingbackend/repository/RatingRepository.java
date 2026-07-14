package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUser_UserIdAndStory_StoryId(Long userId, Long storyId);
    long countByStory_StoryId(Long storyId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.story.storyId = :storyId")
    Double findAverageScoreByStoryId(@Param("storyId") Long storyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Rating r WHERE r.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);
}
