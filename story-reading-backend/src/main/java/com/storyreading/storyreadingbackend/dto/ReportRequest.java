package com.storyreading.storyreadingbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {

    @NotNull(message = "Loại đối tượng bị báo cáo không được để trống")
    private String targetType; // "COMMENT" / "CHAPTER" / "STORY"

    @NotNull(message = "ID đối tượng bị báo cáo không được để trống")
    private Long targetId;

    @NotBlank(message = "Lý do báo cáo không được để trống")
    private String reason;
}