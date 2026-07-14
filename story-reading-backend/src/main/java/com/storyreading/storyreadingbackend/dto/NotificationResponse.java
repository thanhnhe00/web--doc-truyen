package com.storyreading.storyreadingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private String title;
    private String content;
    private String type;
    private Boolean isRead;
    private Long storyId;
    private LocalDateTime createdAt;
}
