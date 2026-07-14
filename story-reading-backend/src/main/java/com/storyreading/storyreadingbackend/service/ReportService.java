package com.storyreading.storyreadingbackend.service;

import com.storyreading.storyreadingbackend.dto.ReportRequest;
import com.storyreading.storyreadingbackend.entity.*;
import com.storyreading.storyreadingbackend.entity.enums.*;
import com.storyreading.storyreadingbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public Report create(ReportRequest request, Authentication authentication) {
        User reporter = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng"));

        ReportTargetType targetType = ReportTargetType.valueOf(request.getTargetType());

        // FR19: Chặn báo cáo trùng lặp khi report trước còn Pending
        reportRepository.findByReporter_UserIdAndTargetTypeAndTargetIdAndStatus(
                reporter.getUserId(), targetType, request.getTargetId(), ReportStatus.PENDING)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Bạn đã báo cáo đối tượng này và đang chờ xử lý");
                });

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(targetType);
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setStatus(ReportStatus.PENDING);

        return reportRepository.save(report);
    }

    public List<Report> getPending() {
        return reportRepository.findByStatus(ReportStatus.PENDING);
    }

    public Report resolve(Long reportId, ReportStatus resolution) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy báo cáo"));
        report.setStatus(resolution); // ví dụ RESOLVED hoặc DISMISSED
        return reportRepository.save(report);
    }
}