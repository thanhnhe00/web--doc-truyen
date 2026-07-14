package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorySearchService {

    private final StoryRepository storyRepository;

    public Page<Story> search(String keyword, Long categoryId, String author, Pageable pageable) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;
        boolean hasAuthor = author != null && !author.isBlank();

        if (hasKeyword && hasCategory && hasAuthor) {
            return storyRepository.findByTitleContainingIgnoreCaseAndCategoryIdAndStatus(
                    keyword, categoryId, ApprovalStatus.PUBLISHED, pageable);
        } else if (hasKeyword && hasAuthor) {
            return storyRepository.findByTitleAndAuthorContainingIgnoreCaseAndStatus(
                    keyword, author, ApprovalStatus.PUBLISHED, pageable);
        } else if (hasCategory && hasAuthor) {
            return storyRepository.findByAuthorAndCategoryIdAndStatus(
                    author, categoryId, ApprovalStatus.PUBLISHED, pageable);
        } else if (hasKeyword && hasCategory) {
            return storyRepository.findByTitleContainingIgnoreCaseAndCategoryIdAndStatus(
                    keyword, categoryId, ApprovalStatus.PUBLISHED, pageable);
        } else if (hasKeyword) {
            return storyRepository.findByTitleContainingIgnoreCaseAndStatus(
                    keyword, ApprovalStatus.PUBLISHED, pageable);
        } else if (hasCategory) {
            return storyRepository.findByCategoryIdAndStatus(
                    categoryId, ApprovalStatus.PUBLISHED, pageable);
        } else if (hasAuthor) {
            return storyRepository.findByAuthorContainingIgnoreCaseAndStatus(
                    author, ApprovalStatus.PUBLISHED, pageable);
        } else {
            return storyRepository.findByStatus(ApprovalStatus.PUBLISHED, pageable);
        }
    }
}
