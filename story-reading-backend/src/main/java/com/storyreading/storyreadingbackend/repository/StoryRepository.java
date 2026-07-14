package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    Page<Story> findByStatus(ApprovalStatus status, Pageable pageable);
    List<Story> findByStatus(ApprovalStatus status);
    Page<Story> findByTitleContainingIgnoreCaseAndStatus(String title, ApprovalStatus status, Pageable pageable);
    Page<Story> findByCreator_UserId(Long creatorId, Pageable pageable);
    List<Story> findByCreator_UserId(Long creatorId);
    List<Story> findByCreator_UserIdAndStatus(Long creatorId, ApprovalStatus status);

    @Query("SELECT DISTINCT s FROM Story s JOIN StoryCategory sc ON sc.story = s WHERE sc.category.categoryId = :categoryId AND s.status = :status")
    Page<Story> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") ApprovalStatus status, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Story s JOIN StoryCategory sc ON sc.story = s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND sc.category.categoryId = :categoryId AND s.status = :status")
    Page<Story> findByTitleContainingIgnoreCaseAndCategoryIdAndStatus(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") ApprovalStatus status,
            Pageable pageable);

    @Query("SELECT DISTINCT s FROM Story s JOIN StoryCategory sc ON sc.story = s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND LOWER(s.author) LIKE LOWER(CONCAT('%', :author, '%')) AND sc.category.categoryId = :categoryId AND s.status = :status")
    Page<Story> findByTitleAndAuthorAndCategoryIdAndStatus(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("categoryId") Long categoryId,
            @Param("status") ApprovalStatus status,
            Pageable pageable);

    @Query("SELECT COUNT(s) FROM Story s WHERE s.creator.userId = :creatorId AND s.status = :status")
    long countByCreator_UserIdAndStatus(@Param("creatorId") Long creatorId, @Param("status") ApprovalStatus status);

    @Query("SELECT DISTINCT s FROM Story s WHERE LOWER(s.author) LIKE LOWER(CONCAT('%', :author, '%')) AND s.status = :status")
    Page<Story> findByAuthorContainingIgnoreCaseAndStatus(@Param("author") String author, @Param("status") ApprovalStatus status, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Story s JOIN StoryCategory sc ON sc.story = s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND LOWER(s.author) LIKE LOWER(CONCAT('%', :author, '%')) AND s.status = :status")
    Page<Story> findByTitleAndAuthorContainingIgnoreCaseAndStatus(@Param("keyword") String keyword, @Param("author") String author, @Param("status") ApprovalStatus status, Pageable pageable);

    @Query("SELECT DISTINCT s FROM Story s JOIN StoryCategory sc ON sc.story = s WHERE LOWER(s.author) LIKE LOWER(CONCAT('%', :author, '%')) AND sc.category.categoryId = :categoryId AND s.status = :status")
    Page<Story> findByAuthorAndCategoryIdAndStatus(@Param("author") String author, @Param("categoryId") Long categoryId, @Param("status") ApprovalStatus status, Pageable pageable);

    List<Story> findByAuthorContainingIgnoreCaseAndStatus(String author, ApprovalStatus status);
}
