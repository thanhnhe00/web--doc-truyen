package com.storyreading.storyreadingbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StoryRequest {

    @NotBlank(message = "Tên truyện không được để trống")
    private String title;

    private String author;
    private String description;
    private String coverImage;
    private Integer ageRating;

    @NotBlank(message = "Loại nội dung không được để trống")
    private String contentType; // "NOVEL" hoặc "COMIC"

    private List<Long> categoryIds;
}