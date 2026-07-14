package com.storyreading.storyreadingbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChapterRequest {

    @NotNull(message = "Số chương không được để trống")
    private Integer chapterNumber;

    private String title;
    private String content; // dùng cho truyện chữ

    private java.util.List<String> imageUrls; // dùng cho truyện tranh, thứ tự = page_number
}