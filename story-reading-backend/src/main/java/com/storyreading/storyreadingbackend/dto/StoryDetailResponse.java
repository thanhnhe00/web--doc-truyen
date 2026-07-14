package com.storyreading.storyreadingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class StoryDetailResponse {
    private Long storyId;
    private String title;
    private String author;
    private String description;
    private String coverImage;
    private Integer ageRating;
    private String contentType;
    private String status;
    private LocalDateTime createdAt;
    private String creatorUsername;
    private List<String> categories;

    private long viewCount;
    private double averageRating;
    private long ratingCount;
    private long followerCount;
    private long commentCount;
    /** true/false nếu đã đăng nhập; null nếu là khách chưa đăng nhập. */
    private Boolean isFollowing;
}
