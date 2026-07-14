package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.CollectionStory;
import com.storyreading.storyreadingbackend.entity.CollectionStoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CollectionStoryRepository extends JpaRepository<CollectionStory, CollectionStoryId> {
    List<CollectionStory> findByCollection_CollectionId(Long collectionId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CollectionStory cs WHERE cs.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);
}
