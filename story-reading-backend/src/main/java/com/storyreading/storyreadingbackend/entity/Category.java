package com.storyreading.storyreadingbackend.entity;

import jakarta.persistence.*;
import lombok.*;

/** Bảng 4.5: categories - Thể loại truyện. */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
}
