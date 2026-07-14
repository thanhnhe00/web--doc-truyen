package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.StoryCategory;
import com.storyreading.storyreadingbackend.entity.StoryCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StoryCategoryRepository extends JpaRepository<StoryCategory, StoryCategoryId> {
    List<StoryCategory> findByStory_StoryId(Long storyId);
    List<StoryCategory> findByCategory_CategoryId(Long categoryId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StoryCategory sc WHERE sc.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);
}
