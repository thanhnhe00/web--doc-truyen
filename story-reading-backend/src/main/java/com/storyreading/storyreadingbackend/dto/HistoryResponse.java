package com.storyreading.storyreadingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class HistoryResponse {
    private Long storyId;
    private String storyTitle;
    private String coverImage;
    private Long chapterId;
    private Integer chapterNumber;
    private String chapterTitle;
    private LocalDateTime updatedAt;
}
