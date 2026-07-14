package com.storyreading.storyreadingbackend.repository;

import com.storyreading.storyreadingbackend.entity.Report;
import com.storyreading.storyreadingbackend.entity.enums.ReportStatus;
import com.storyreading.storyreadingbackend.entity.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatus(ReportStatus status);
    Page<Report> findByStatusOrderByCreatedAtAsc(ReportStatus status, Pageable pageable);
    Optional<Report> findByReporter_UserIdAndTargetTypeAndTargetIdAndStatus(
            Long reporterId, ReportTargetType targetType, Long targetId, ReportStatus status);
}