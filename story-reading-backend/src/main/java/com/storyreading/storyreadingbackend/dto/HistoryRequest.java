package com.storyreading.storyreadingbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryRequest {
    private Long chapterId;
    private Integer scrollPosition;
}