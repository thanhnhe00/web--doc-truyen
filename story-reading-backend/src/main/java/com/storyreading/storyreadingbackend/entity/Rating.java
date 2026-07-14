package com.storyreading.storyreadingbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Bảng 4.12: ratings - Đánh giá số sao (FR06 mở rộng). */
@Entity
@Table(name = "ratings", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_story_rating", columnNames = {"user_id", "story_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long ratingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    /** Điểm đánh giá 1-5 sao; ràng buộc CHECK được thực thi ở tầng DB (chk_rating_score). */
    @Column(name = "score", nullable = false)
    private Short score;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
