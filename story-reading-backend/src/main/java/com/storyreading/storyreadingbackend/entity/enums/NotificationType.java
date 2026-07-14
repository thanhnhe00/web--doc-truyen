package com.storyreading.storyreadingbackend.entity.enums;

/** Phân loại thông báo hệ thống gửi tới người dùng (Bảng 4.14). */
public enum NotificationType {
    STORY_APPROVED,
    STORY_REJECTED,
    CHAPTER_APPROVED,
    CHAPTER_REJECTED,
    NEW_CHAPTER,
    CONTENT_HIDDEN
}
