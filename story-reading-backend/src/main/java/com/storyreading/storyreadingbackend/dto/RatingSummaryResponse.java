package com.storyreading.storyreadingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RatingSummaryResponse {
    /** Điểm trung bình, làm tròn 1 chữ số thập phân. 0 nếu chưa có đánh giá nào. */
    private Double averageScore;
    private Long ratingCount;
    /** Điểm mà người dùng hiện tại đã chấm; null nếu chưa đăng nhập hoặc chưa chấm. */
    private Short myScore;
}
