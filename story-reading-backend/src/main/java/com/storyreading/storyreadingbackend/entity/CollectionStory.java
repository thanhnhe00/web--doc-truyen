package com.storyreading.storyreadingbackend.entity;

import jakarta.persistence.*;
import lombok.*;

/** Bảng 4.8: collection_stories - Liên kết Nhiều-Nhiều giữa ThemeCollection và Story. */
@Entity
@Table(name = "collection_stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(CollectionStoryId.class)
public class CollectionStory {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private ThemeCollection collection;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;
}
