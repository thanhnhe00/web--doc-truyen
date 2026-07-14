package com.storyreading.storyreadingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChapterSummaryResponse {
    private Long chapterId;
    private Integer chapterNumber;
    private String title;
    private LocalDateTime createdAt;
}