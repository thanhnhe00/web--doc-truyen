package com.storyreading.storyreadingbackend.entity;

import jakarta.persistence.*;
import lombok.*;

/** Bảng 4.6: story_categories - Liên kết Nhiều-Nhiều giữa Story và Category. */
@Entity
@Table(name = "story_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(StoryCategoryId.class)
public class StoryCategory {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
