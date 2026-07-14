package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.entity.Story;
import com.storyreading.storyreadingbackend.entity.enums.ApprovalStatus;
import com.storyreading.storyreadingbackend.repository.StoryRepository;
import com.storyreading.storyreadingbackend.repository.ViewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendingService {

    private final ViewLogRepository viewLogRepository;
    private final StoryRepository storyRepository;

    @Cacheable(value = "trending", key = "#days + '-' + #limit")
    public List<Story> getTrending(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Story> trending = viewLogRepository.findTrendingStories(since, PageRequest.of(0, limit));
        if (trending != null && !trending.isEmpty()) {
            return trending;
        }
        return storyRepository.findByStatus(ApprovalStatus.PUBLISHED, PageRequest.of(0, limit)).getContent();
    }

    @Scheduled(fixedRate = 1800000)
    @CacheEvict(value = "trending", allEntries = true)
    public void refreshTrendingCache() {
    }
}
