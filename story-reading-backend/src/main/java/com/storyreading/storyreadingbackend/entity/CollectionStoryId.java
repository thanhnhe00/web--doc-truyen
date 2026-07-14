package com.storyreading.storyreadingbackend.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/** Khóa chính tổ hợp (Composite PK) cho bảng trung gian collection_stories. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStoryId implements Serializable {

    private Long collection;
    private Long story;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionStoryId)) return false;
        CollectionStoryId that = (CollectionStoryId) o;
        return Objects.equals(collection, that.collection) && Objects.equals(story, that.story);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection, story);
    }
}
