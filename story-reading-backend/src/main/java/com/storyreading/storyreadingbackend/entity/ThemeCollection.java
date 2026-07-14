package com.storyreading.storyreadingbackend.entity;

import jakarta.persistence.*;
import lombok.*;

/** Bảng 4.7: theme_collections - Bộ sưu tập chủ đề (FR18). */
@Entity
@Table(name = "theme_collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_id")
    private Long collectionId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
}
