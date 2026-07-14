package com.storyreading.storyreadingbackend.entity.enums;

/**
 * Trạng thái luồng kiểm duyệt/xuất bản, dùng chung cho cả Story và Chapter
 * (mục 2.2.3 - Quy trình kiểm duyệt và xuất bản nội dung / Story Approval Workflow).
 */
public enum ApprovalStatus {
    DRAFT,
    PENDING,
    PUBLISHED,
    REJECTED,
    HIDDEN
}
