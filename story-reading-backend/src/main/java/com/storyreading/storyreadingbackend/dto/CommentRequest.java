package com.storyreading.storyreadingbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;

    private Long parentId; // null nếu là bình luận gốc
}