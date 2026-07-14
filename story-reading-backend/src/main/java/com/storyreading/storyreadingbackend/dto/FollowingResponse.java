package com.storyreading.storyreadingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowingResponse {
    private Long storyId;
    private String title;
    private String author;
    private String coverImage;
    private String status;
    private String contentType;
}
