package com.storyreading.storyreadingbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ThemeCollectionRequest {

    @NotBlank(message = "Tên bộ sưu tập không được để trống")
    private String name;

    private String description;
    private List<Long> storyIds;
}