package com.storyreading.storyreadingbackend.entity;

import jakarta.persistence.*;
import lombok.*;

/** Bảng 4.4: chapter_images - Ảnh từng trang của chương (truyện tranh). */
@Entity
@Table(name = "chapter_images", uniqueConstraints = {
        @UniqueConstraint(name = "uq_chapter_page", columnNames = {"chapter_id", "page_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_image_id")
    private Long chapterImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;
}
