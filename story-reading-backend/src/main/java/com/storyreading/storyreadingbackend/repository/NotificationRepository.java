package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByReceiver_UserIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);
    long countByReceiver_UserIdAndIsReadFalse(Long receiverId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.story.storyId = :storyId")
    void deleteByStoryId(Long storyId);
}
