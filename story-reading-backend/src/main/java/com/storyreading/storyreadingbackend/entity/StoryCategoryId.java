package com.storyreading.storyreadingbackend.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/** Khóa chính tổ hợp (Composite PK) cho bảng trung gian story_categories. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryCategoryId implements Serializable {

    private Long story;
    private Long category;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoryCategoryId)) return false;
        StoryCategoryId that = (StoryCategoryId) o;
        return Objects.equals(story, that.story) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(story, category);
    }
}
