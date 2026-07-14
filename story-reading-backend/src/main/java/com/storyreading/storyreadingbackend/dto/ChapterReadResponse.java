package com.storyreading.storyreadingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChapterReadResponse {
    private Long chapterId;
    private Integer chapterNumber;
    private String title;
    private String content;
    private List<String> imageUrls;
    private Long storyId;
    private String storyTitle;
    private String contentType;
    /** null nếu đây đã là chương đầu tiên đã xuất bản. */
    private Long prevChapterId;
    /** null nếu đây đã là chương mới nhất đã xuất bản. */
    private Long nextChapterId;
}