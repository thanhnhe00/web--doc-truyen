package com.storyreading.storyreadingbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** Bảng 4.9: history - Lịch sử đọc & Tự động tiếp tục đọc (FR08). */
@Entity
@Table(name = "history", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_story_history", columnNames = {"user_id", "story_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "scroll_position")
    @Builder.Default
    private Integer scrollPosition = 0;

    @Column(name = "is_prompted", nullable = false)
    @Builder.Default
    private Boolean isPrompted = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
