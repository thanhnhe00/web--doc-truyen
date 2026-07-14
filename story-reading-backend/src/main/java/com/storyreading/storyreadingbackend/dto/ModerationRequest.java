package com.storyreading.storyreadingbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModerationRequest {
    private String reason; // bắt buộc khi từ chối/ẩn, không bắt buộc khi duyệt
}